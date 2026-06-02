package com.cyan.exam.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ExamQuestionMapper {
    int batchInsertExamQuestion(@Param("examId") Long examId, @Param("questionIds") List<Long> questionIds);
    int deleteExamQuestionByExamId(Long examId);
    List<Long> selectQuestionIdsByExamId(Long examId);
}