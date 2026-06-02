package com.cyan.exam.system.service.impl;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cyan.exam.system.domain.CyanExamInfo;
import com.cyan.exam.system.mapper.CyanExamInfoMapper;
import com.cyan.exam.system.mapper.ExamStudentMapper;
import com.cyan.exam.system.mapper.ExamQuestionMapper;
import com.cyan.exam.system.service.ICyanExamInfoService;

@Service
public class CyanExamInfoServiceImpl implements ICyanExamInfoService {

    @Autowired
    private CyanExamInfoMapper cyanExamInfoMapper;

    @Autowired
    private ExamStudentMapper examStudentMapper;

    @Autowired
    private ExamQuestionMapper examQuestionMapper;

    @Override
    public List<CyanExamInfo> selectCyanExamInfoList(CyanExamInfo examInfo) {
        return cyanExamInfoMapper.selectCyanExamInfoList(examInfo);
    }

    @Override
    public CyanExamInfo selectCyanExamInfoByExamId(Long examId) {
        return cyanExamInfoMapper.selectCyanExamInfoByExamId(examId);
    }

    @Override
    @Transactional
    public int insertCyanExamInfo(CyanExamInfo examInfo) {
        int rows = cyanExamInfoMapper.insertCyanExamInfo(examInfo);
        // 处理学生关联
        saveExamStudents(examInfo.getExamId(),
                examInfo.getStudentIds() != null ? Arrays.asList(examInfo.getStudentIds()) : null);
        // 处理试题关联
        saveExamQuestions(examInfo.getExamId(),
                examInfo.getQuestionIds() != null ? Arrays.asList(examInfo.getQuestionIds()) : null);
        return rows;
    }

    @Override
    @Transactional
    public int updateCyanExamInfo(CyanExamInfo examInfo) {
        int rows = cyanExamInfoMapper.updateCyanExamInfo(examInfo);
        // 更新关联：先删后增
        if (examInfo.getStudentIds() != null) {
            saveExamStudents(examInfo.getExamId(), Arrays.asList(examInfo.getStudentIds()));
        }
        if (examInfo.getQuestionIds() != null) {
            saveExamQuestions(examInfo.getExamId(), Arrays.asList(examInfo.getQuestionIds()));
        }
        return rows;
    }

    @Override
    @Transactional
    public int deleteCyanExamInfoByExamIds(Long[] examIds) {
        // 删除考试时也需要删除关联
        for (Long examId : examIds) {
            examStudentMapper.deleteExamStudentByExamId(examId);
            examQuestionMapper.deleteExamQuestionByExamId(examId);
        }
        return cyanExamInfoMapper.deleteCyanExamInfoByExamIds(examIds);
    }

    @Override
    public List<Long> selectStudentIdsByExamId(Long examId) {
        return examStudentMapper.selectUserIdsByExamId(examId);
    }

    @Override
    @Transactional
    public void saveExamStudents(Long examId, List<Long> userIds) {
        examStudentMapper.deleteExamStudentByExamId(examId);
        if (userIds != null && !userIds.isEmpty()) {
            examStudentMapper.batchInsertExamStudent(examId, userIds);
        }
    }

    @Override
    public List<Long> selectQuestionIdsByExamId(Long examId) {
        return examQuestionMapper.selectQuestionIdsByExamId(examId);
    }

    @Override
    @Transactional
    public void saveExamQuestions(Long examId, List<Long> questionIds) {
        examQuestionMapper.deleteExamQuestionByExamId(examId);
        if (questionIds != null && !questionIds.isEmpty()) {
            examQuestionMapper.batchInsertExamQuestion(examId, questionIds);
        }
    }
}