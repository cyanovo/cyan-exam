package com.cyan.exam.system.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cyan.exam.system.domain.*;
import com.cyan.exam.system.mapper.*;
import com.cyan.exam.system.service.IExamStudentService;

@Service
public class ExamStudentServiceImpl implements IExamStudentService {

    @Autowired
    private CyanExamInfoMapper examInfoMapper;
    @Autowired
    private ExamStudentMapper examStudentMapper;
    @Autowired
    private ExamQuestionMapper examQuestionMapper;
    @Autowired
    private CyanQuestionMapper questionMapper;    // 题库 Mapper（你已有的）
    @Autowired
    private ExamAnswerRecordMapper answerRecordMapper;

    @Override
    public Map<String, Object> getWaitInfo(Long userId) {
        Map<String, Object> result = new HashMap<>();
        // 查询学生已分配的所有考试ID
        List<Long> examIds = examStudentMapper.selectExamIdsByUserId(userId);  // 需补充
        if (examIds.isEmpty()) {
            result.put("status", "none");
            return result;
        }
        // 获取这些考试中正在进行或即将开始的
        Date now = new Date();
        List<CyanExamInfo> exams = examInfoMapper.selectCyanExamInfoList(new CyanExamInfo());
        exams = exams.stream()
                .filter(e -> examIds.contains(e.getExamId()) && e.getEndTime().after(now))
                .sorted(Comparator.comparing(CyanExamInfo::getStartTime))
                .collect(Collectors.toList());

        if (exams.isEmpty()) {
            result.put("status", "none");
            return result;
        }
        CyanExamInfo target = exams.get(0);
        if (target.getStartTime().before(now) && target.getEndTime().after(now)) {
            result.put("status", "ongoing");
            result.put("examId", target.getExamId());
            result.put("examName", target.getExamName());
            result.put("endTime", target.getEndTime());
        } else if (target.getStartTime().after(now)) {
            result.put("status", "upcoming");
            result.put("examId", target.getExamId());
            result.put("examName", target.getExamName());
            result.put("startTime", target.getStartTime());
            result.put("endTime", target.getEndTime());
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getQuestionsWithAnswers(Long examId, Long userId) {
        List<Long> qIds = examQuestionMapper.selectQuestionIdsByExamId(examId);
        if (qIds.isEmpty()) return Collections.emptyList();
        List<CyanQuestion> questions = questionMapper.selectCyanQuestionByQuestionIds(qIds.toArray(new Long[0])); // 需批量查询
        List<ExamAnswerRecord> records = answerRecordMapper.selectByExamAndUser(examId, userId);
        Map<Long, String> answerMap = records.stream()
                .collect(Collectors.toMap(ExamAnswerRecord::getQuestionId, ExamAnswerRecord::getAnswer));

        List<Map<String, Object>> result = new ArrayList<>();
        for (CyanQuestion q : questions) {
            Map<String, Object> item = new HashMap<>();
            item.put("questionId", q.getQuestionId());
            item.put("title", q.getTitle());
            item.put("questionType", q.getQuestionType());
            item.put("difficulty", q.getDifficulty());
            item.put("category", q.getCategory());
            if (q.getSubTitle() != null && !q.getSubTitle().isEmpty()) {
                item.put("options", JSON.parseArray(q.getSubTitle()));  // 解析 JSON 数组
            }
            item.put("userAnswer", answerMap.getOrDefault(q.getQuestionId(), ""));
            result.add(item);
        }
        return result;
    }

    @Override
    @Transactional
    public void submitAnswer(Long examId, Long userId, Long questionId, String answer) {
        // 校验考试时间（略）
        ExamAnswerRecord record = new ExamAnswerRecord();
        record.setExamId(examId);
        record.setUserId(userId);
        record.setQuestionId(questionId);
        record.setAnswer(answer);
        answerRecordMapper.insertOrUpdate(record);
    }
}