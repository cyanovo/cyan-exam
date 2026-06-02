package com.cyan.exam.system.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.cyan.exam.system.service.IExamGradingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson.JSON;
import com.cyan.exam.common.core.domain.entity.SysUser;
import com.cyan.exam.system.domain.*;
import com.cyan.exam.system.mapper.*;

@Service
public class ExamGradingServiceImpl implements IExamGradingService {

    @Autowired
    private CyanExamInfoMapper examInfoMapper;
    @Autowired
    private ExamStudentMapper examStudentMapper;
    @Autowired
    private ExamQuestionMapper examQuestionMapper;
    @Autowired
    private CyanQuestionMapper questionMapper;
    @Autowired
    private ExamAnswerRecordMapper answerRecordMapper;
    @Autowired
    private SysUserMapper userMapper;   // 新增：用于查询学生姓名

    @Override
    public List<Map<String, Object>> getExamListForGrading() {
        List<CyanExamInfo> exams = examInfoMapper.selectCyanExamInfoList(new CyanExamInfo());
        List<Map<String, Object>> result = new ArrayList<>();
        for (CyanExamInfo exam : exams) {
            Map<String, Object> map = new HashMap<>();
            map.put("examId", exam.getExamId());
            map.put("examName", exam.getExamName());
            map.put("startTime", exam.getStartTime());
            map.put("endTime", exam.getEndTime());
            result.add(map);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getStudentProgress(Long examId) {
        List<Long> userIds = examStudentMapper.selectUserIdsByExamId(examId);
        int totalQuestions = examQuestionMapper.selectQuestionIdsByExamId(examId).size();

        List<Map<String, Object>> progressList = new ArrayList<>();
        for (Long userId : userIds) {
            List<ExamAnswerRecord> records = answerRecordMapper.selectByExamAndUser(examId, userId);
            int answered = records.size();
            int graded = (int) records.stream().filter(r -> r.getScore() != null && r.getScore() > 0).count();
            SysUser user = userMapper.selectUserById(userId);
            String studentName = user != null ? user.getNickName() : "未知";
            Map<String, Object> map = new HashMap<>();
            map.put("userId", userId);
            map.put("studentName", studentName);
            map.put("answered", answered);
            map.put("total", totalQuestions);
            map.put("graded", graded);
            progressList.add(map);
        }
        return progressList;
    }

    @Override
    public Map<String, Object> getStudentAnswerDetail(Long examId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        CyanExamInfo exam = examInfoMapper.selectCyanExamInfoByExamId(examId);
        result.put("examName", exam.getExamName());

        List<Long> qIds = examQuestionMapper.selectQuestionIdsByExamId(examId);
        List<CyanQuestion> questions = questionMapper.selectCyanQuestionByQuestionIds(qIds.toArray(new Long[0]));

        List<ExamAnswerRecord> records = answerRecordMapper.selectByExamAndUser(examId, userId);
        Map<Long, ExamAnswerRecord> recordMap = records.stream()
                .collect(Collectors.toMap(ExamAnswerRecord::getQuestionId, r -> r));

        List<Map<String, Object>> questionDetails = new ArrayList<>();
        for (CyanQuestion q : questions) {
            Map<String, Object> item = new HashMap<>();
            item.put("questionId", q.getQuestionId());
            item.put("title", q.getTitle());
            item.put("type", q.getQuestionType());
            item.put("referenceAnswer", q.getAnswer());
            if ("SINGLE_CHOICE".equals(q.getQuestionType()) || "MULTI_CHOICE".equals(q.getQuestionType())
                    || "JUDGE".equals(q.getQuestionType())) {
                if (q.getSubTitle() != null && !q.getSubTitle().isEmpty()) {
                    item.put("options", JSON.parseArray(q.getSubTitle()));
                }
            }
            ExamAnswerRecord record = recordMap.get(q.getQuestionId());
            item.put("studentAnswer", record != null ? record.getAnswer() : "");
            item.put("score", record != null && record.getScore() != null ? record.getScore() : null);
            item.put("recordId", record != null ? record.getRecordId() : null);

            boolean autoGradable = "SINGLE_CHOICE".equals(q.getQuestionType())
                    || "JUDGE".equals(q.getQuestionType());
            item.put("autoGradable", autoGradable);

            questionDetails.add(item);
        }

        result.put("questions", questionDetails);
        // 可选：增加学生姓名
        SysUser student = userMapper.selectUserById(userId);
        result.put("studentName", student != null ? student.getNickName() : "未知");
        return result;
    }

    @Override
    @Transactional
    public void submitScores(List<Map<String, Object>> scoreList) {
        for (Map<String, Object> map : scoreList) {
            Long recordId = Long.valueOf(map.get("recordId").toString());
            Integer score = Integer.valueOf(map.get("score").toString());
            ExamAnswerRecord record = new ExamAnswerRecord();
            record.setRecordId(recordId);
            record.setScore(score);
            answerRecordMapper.updateExamAnswerRecord(record);
        }
    }
}