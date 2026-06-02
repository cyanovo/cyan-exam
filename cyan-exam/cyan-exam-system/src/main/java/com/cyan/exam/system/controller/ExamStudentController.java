package com.cyan.exam.web.controller.exam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.cyan.exam.common.core.controller.BaseController;
import com.cyan.exam.common.core.domain.AjaxResult;
import com.cyan.exam.system.service.IExamStudentService;
import com.cyan.exam.common.utils.SecurityUtils;
import java.util.Map;

@RestController
@RequestMapping("/exam/student")
public class ExamStudentController extends BaseController {

    @Autowired
    private IExamStudentService examStudentService;

    @GetMapping("/waitInfo")
    public AjaxResult waitInfo() {
        Long userId = SecurityUtils.getUserId();
        Map<String, Object> data = examStudentService.getWaitInfo(userId);
        return success(data);
    }

    @GetMapping("/questions")
    public AjaxResult questions(@RequestParam Long examId) {
        Long userId = SecurityUtils.getUserId();
        return success(examStudentService.getQuestionsWithAnswers(examId, userId));
    }

    @PostMapping("/submitAnswer")
    public AjaxResult submitAnswer(@RequestBody Map<String, Object> params) {
        Long examId = Long.valueOf(params.get("examId").toString());
        Long questionId = Long.valueOf(params.get("questionId").toString());
        String answer = params.get("answer").toString();
        examStudentService.submitAnswer(examId, SecurityUtils.getUserId(), questionId, answer);
        return success();
    }
}