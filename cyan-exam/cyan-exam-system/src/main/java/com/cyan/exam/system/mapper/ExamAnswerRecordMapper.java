package com.cyan.exam.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.cyan.exam.system.domain.ExamAnswerRecord;

public interface ExamAnswerRecordMapper {
    List<ExamAnswerRecord> selectExamAnswerRecordList(ExamAnswerRecord record);
    ExamAnswerRecord selectByExamUserQuestion(ExamAnswerRecord record);
    List<ExamAnswerRecord> selectByExamAndUser(@Param("examId") Long examId, @Param("userId") Long userId);
    int insertOrUpdate(ExamAnswerRecord record);
    int updateExamAnswerRecord(ExamAnswerRecord record);
    int deleteExamAnswerRecordByRecordId(Long recordId);
    int deleteExamAnswerRecordByRecordIds(Long[] recordIds);
    int deleteByExamAndUser(@Param("examId") Long examId, @Param("userId") Long userId);
}