package com.example.english.demo.service;

import com.example.english.demo.dto.request.ToeicQuestionCreateRequest;
import com.example.english.demo.dto.request.ToeicQuestionUpdateRequest;
import com.example.english.demo.dto.response.ToeicQuestionResponse;
import com.example.english.demo.entity.ToeicExam;
import com.example.english.demo.entity.ToeicQuestion;
import com.example.english.demo.exception.AppException;
import com.example.english.demo.exception.ErrorCode;
import com.example.english.demo.mapper.ToeicQuestionMapper;
import com.example.english.demo.repository.ToeicExamRepository;
import com.example.english.demo.repository.ToeicQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class ToeicQuestionService {
   private final ToeicQuestionMapper toeicQuestionMapper;
   private final ToeicQuestionRepository toeicQuestionRepository;
   private final ToeicExamRepository toeicExamRepository;

   @Value("${upload.path}") // Đọc thư mục lưu trữ ảnh từ application.properties
   private String uploadDir;

//    muốn nhận vào list thì phải có cái gọi là sửa thành 1 hàm list
    public List<ToeicQuestionResponse> createToeicQuestions(List<ToeicQuestionCreateRequest> toeicQuestionCreateRequests, Long examId, MultipartFile[] images) {
        ToeicExam toeicExam = toeicExamRepository.findById(examId)
                .orElseThrow(() -> new AppException(ErrorCode.TOEIC_EXAM_NOT_EXITSTED));
        List<ToeicQuestionResponse> responses = new ArrayList<>();

        for (ToeicQuestionCreateRequest toeicQuestionCreateRequest : toeicQuestionCreateRequests) {
            if (toeicQuestionRepository.existsByQuestionTextAndCorrectAnswerAndToeicExam_ExamId(
                    toeicQuestionCreateRequest.getQuestionText(),
                    toeicQuestionCreateRequest.getCorrectAnswer(),
                    examId)) {
                throw new AppException(ErrorCode.TOEIC_QUESTION_EXITSTED);
            }
        }

        // Tạo câu hỏi và lưu ảnh
        for (int i = 0; i < toeicQuestionCreateRequests.size(); i++) {
            ToeicQuestionCreateRequest toeicQuestionCreateRequest = toeicQuestionCreateRequests.get(i);
            ToeicQuestion toeicQuestion = toeicQuestionMapper.toToeicQuestion(toeicQuestionCreateRequest);
            toeicQuestion.setToeicExam(toeicExam);

            // Nếu có ảnh, lưu ảnh và lưu đường dẫn ảnh vào cơ sở dữ liệu
            if (images != null && images.length > i) {
                MultipartFile image = images[i]; // Lấy ảnh theo thứ tự từ danh sách

                try {
                    // Tạo thư mục cho examId nếu chưa tồn tại
                    String examFolderPath = "C:/Users/DELL/Downloads/demoEnglishSpringBoot/src/main/resources/static/images/toeicTest" + examId;
                    Path examFolder = Paths.get(examFolderPath);
                    if (!Files.exists(examFolder)) {
                        Files.createDirectories(examFolder); // Tạo thư mục nếu chưa có
                    }
                    // Lưu ảnh vào thư mục theo examId
                    String originalImageName = image.getOriginalFilename();
                    String imageName = System.currentTimeMillis() + "_" + originalImageName.replaceAll(" ", "_"); // Thay dấu cách bằng dấu gạch dưới
                    Path imagePath = examFolder.resolve(imageName);
                    Files.copy(image.getInputStream(), imagePath);

                    // Lưu đường dẫn ảnh vào cơ sở dữ liệu (đường dẫn tương đối)
                    toeicQuestion.setImage("/images/toeicTest" + examId + "/" + imageName);
//                    toeicQuestion.setImage(imageName);
                } catch (IOException e) {
                    throw new AppException(ErrorCode.FILE_UPLOAD_ERROR);
                }
            }
            toeicQuestion = toeicQuestionRepository.save(toeicQuestion);
            ToeicQuestionResponse response = toeicQuestionMapper.toToeicQuestionResponse(toeicQuestion);
            response.setImage(toeicQuestion.getImage());
            responses.add(response);
        }
        return responses;
    }

   public ToeicQuestionResponse updateToeicQuestion(Long questionId, ToeicQuestionUpdateRequest request){
       ToeicQuestion toeicQuestion=toeicQuestionRepository.findById(questionId)
               .orElseThrow(()->new RuntimeException("Question not found"));
       toeicQuestionMapper.updateToeicQuestion(toeicQuestion,request);
       ToeicQuestion updatedToeicQuestion = toeicQuestionRepository.save(toeicQuestion);
       return toeicQuestionMapper.toToeicQuestionResponse(updatedToeicQuestion);
   }


    public List<ToeicQuestionResponse> getToeicQuestionsByPart(Long examId, Integer part) {
        List<ToeicQuestion> toeicQuestionsPage = toeicQuestionRepository.findByToeicExam_ExamIdAndPart(examId, part);
        return toeicQuestionsPage.stream()
                .map(toeicQuestionMapper::toToeicQuestionResponse)
                .collect(Collectors.toList());
    }

    public List<ToeicQuestionResponse> importToeicQuestionsFromExcel(MultipartFile file, Long examId) {
        List<ToeicQuestionResponse> responses = new ArrayList<>();

        ToeicExam toeicExam = toeicExamRepository.findById(examId)
                .orElseThrow(() -> new AppException(ErrorCode.TOEIC_EXAM_NOT_EXITSTED));

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                String questionText = getCellValue(row.getCell(0));
                String correctAnswer = getCellValue(row.getCell(5));

                boolean isExist = toeicQuestionRepository
                        .existsByQuestionTextAndCorrectAnswerAndToeicExam_ExamId(questionText, correctAnswer, examId);

                if (isExist) {
                    throw new AppException(ErrorCode.TOEIC_QUESTION_EXITSTED);
                }

                ToeicQuestionCreateRequest request = ToeicQuestionCreateRequest.builder()
                        .questionText(questionText)
                        .dapAn1(getCellValue(row.getCell(1)))
                        .dapAn2(getCellValue(row.getCell(2)))
                        .dapAn3(getCellValue(row.getCell(3)))
                        .dapAn4(getCellValue(row.getCell(4)))
                        .correctAnswer(correctAnswer)
                        .part((int) row.getCell(6).getNumericCellValue())
                        .build();

                ToeicQuestion question = toeicQuestionMapper.toToeicQuestion(request);
                question.setToeicExam(toeicExam);
                toeicQuestionRepository.save(question);

                responses.add(toeicQuestionMapper.toToeicQuestionResponse(question));
            }

        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_ERROR);
        }

        return responses;
    }


//    public List<ToeicQuestionResponse> importToeicQuestionsFromExcel(MultipartFile file, Long examId) {
//        List<ToeicQuestionResponse> responses = new ArrayList<>();
//
//        ToeicExam toeicExam = toeicExamRepository.findById(examId)
//                .orElseThrow(() -> new AppException(ErrorCode.TOEIC_EXAM_NOT_EXITSTED));
//
//        try (InputStream inputStream = file.getInputStream();
//             Workbook workbook = new XSSFWorkbook(inputStream)) {
//
//            Sheet sheet = workbook.getSheetAt(0);
//
//            for (Row row : sheet) {
//                if (row.getRowNum() == 0) continue;
//
//                String questionText = getCellValue(row.getCell(0));
//                String dapAn1 = getCellValue(row.getCell(1));
//                String dapAn2 = getCellValue(row.getCell(2));
//                String dapAn3 = getCellValue(row.getCell(3));
//                String dapAn4 = getCellValue(row.getCell(4)); // có thể trống
//                String correctAnswer = getCellValue(row.getCell(5));
//
//                if (questionText.isBlank() || dapAn1.isBlank() || dapAn2.isBlank()
//                        || dapAn3.isBlank() || correctAnswer.isBlank()) {
//                    continue; // bỏ dòng thiếu thông tin bắt buộc
//                }
//
//                int part = (int) row.getCell(6).getNumericCellValue();
//
//                ToeicQuestionCreateRequest request = ToeicQuestionCreateRequest.builder()
//                        .questionText(questionText)
//                        .dapAn1(dapAn1)
//                        .dapAn2(dapAn2)
//                        .dapAn3(dapAn3)
//                        .dapAn4(dapAn4)
//                        .correctAnswer(correctAnswer)
//                        .part(part)
//                        .build();
//
//                ToeicQuestion question = toeicQuestionMapper.toToeicQuestion(request);
//                question.setToeicExam(toeicExam);
//                toeicQuestionRepository.save(question);
//
//                responses.add(toeicQuestionMapper.toToeicQuestionResponse(question));
//            }
//
//        } catch (IOException e) {
//            throw new AppException(ErrorCode.FILE_UPLOAD_ERROR);
//        }
//
//        return responses;
//    }


    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }


}
