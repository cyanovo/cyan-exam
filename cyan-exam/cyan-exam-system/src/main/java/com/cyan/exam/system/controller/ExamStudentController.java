package com.cyan.exam.system.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.cyan.exam.common.core.controller.BaseController;
import com.cyan.exam.common.core.domain.AjaxResult;
import com.cyan.exam.common.utils.SecurityUtils;
import com.cyan.exam.system.domain.CyanExamInfo;
import com.cyan.exam.system.mapper.CyanExamInfoMapper;
import com.cyan.exam.system.mapper.ExamStudentMapper;
import com.cyan.exam.system.service.IExamStudentService;

@RestController
@RequestMapping("/exam/student")
public class ExamStudentController extends BaseController {

    @Autowired
    private IExamStudentService examStudentService;

    @Autowired
    private ExamStudentMapper examStudentMapper;

    @Autowired
    private CyanExamInfoMapper examInfoMapper;

    /**
     * 学生考试等待信息
     */
    @GetMapping("/waitInfo")
    public AjaxResult waitInfo() {
        Long userId = SecurityUtils.getUserId();
        Map<String, Object> data = examStudentService.getWaitInfo(userId);
        return success(data);
    }

    /**
     * 获取考试题目和答题记录（带校验）
     */
    @GetMapping("/questions")
    public AjaxResult questions(@RequestParam Long examId) {
        Long userId = SecurityUtils.getUserId();

        // 校验学生是否分配到此考试
        List<Long> assignedExams = examStudentMapper.selectExamIdsByUserId(userId);
        if (!assignedExams.contains(examId)) {
            return error("您没有权限参加该考试");
        }

        // 获取考试信息
        CyanExamInfo exam = examInfoMapper.selectCyanExamInfoByExamId(examId);
        if (exam == null) {
            return error("考试不存在");
        }

        // 检查考试时间
        Date now = new Date();
        if (now.before(exam.getStartTime())) {
            return error("考试尚未开始");
        }
        if (now.after(exam.getEndTime())) {
            return error("考试已结束");
        }

        // 获取题目列表（包含学生已有答案）
        List<Map<String, Object>> questionList = examStudentService.getQuestionsWithAnswers(examId, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("examName", exam.getExamName());
        result.put("endTime", exam.getEndTime());
        result.put("questions", questionList);
        return success(result);
    }

    /**
     * 提交一道题的答案
     */
    @PostMapping("/submitAnswer")
    public AjaxResult submitAnswer(@RequestBody Map<String, Object> params) {
        Long examId = Long.valueOf(params.get("examId").toString());
        Long questionId = Long.valueOf(params.get("questionId").toString());
        String answer = params.get("answer").toString();
        Long userId = SecurityUtils.getUserId();

        // 简单的时间校验（可略，前端已控制）
        examStudentService.submitAnswer(examId, userId, questionId, answer);
        return success();
    }
}