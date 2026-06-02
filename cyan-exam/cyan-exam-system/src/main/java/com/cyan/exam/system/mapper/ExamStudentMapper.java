package com.cyan.exam.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ExamStudentMapper {
    /**
     * 批量插入考试-学生关联
     */
    int batchInsertExamStudent(@Param("examId") Long examId, @Param("userIds") List<Long> userIds);

    /**
     * 根据考试ID删除所有学生关联
     */
    int deleteExamStudentByExamId(Long examId);

    /**
     * 根据考试ID查询已分配的学生userId列表
     */
    List<Long> selectUserIdsByExamId(Long examId);
}