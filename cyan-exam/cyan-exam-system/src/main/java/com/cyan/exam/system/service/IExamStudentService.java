package com.cyan.exam.system.service;

import java.util.List;
import java.util.Map;

public interface IExamStudentService {
    Map<String, Object> getWaitInfo(Long userId);
    List<Map<String, Object>> getQuestionsWithAnswers(Long examId, Long userId);
    void submitAnswer(Long examId, Long userId, Long questionId, String answer);
}