package com.cyan.exam.system.mapper;

import java.util.List;
import com.cyan.exam.system.domain.CyanQuestion;

/**
 * 题库Mapper接口
 * 
 * @author cyan
 * @date 2026-05-26
 */
public interface CyanQuestionMapper 
{
    /**
     * 查询题库
     * 
     * @param questionId 题库主键
     * @return 题库
     */
    public CyanQuestion selectCyanQuestionByQuestionId(Long questionId);

    /**
     * 查询题库列表
     * 
     * @param cyanQuestion 题库
     * @return 题库集合
     */
    public List<CyanQuestion> selectCyanQuestionList(CyanQuestion cyanQuestion);

    /**
     * 新增题库
     * 
     * @param cyanQuestion 题库
     * @return 结果
     */
    public int insertCyanQuestion(CyanQuestion cyanQuestion);

    /**
     * 修改题库
     * 
     * @param cyanQuestion 题库
     * @return 结果
     */
    public int updateCyanQuestion(CyanQuestion cyanQuestion);

    /**
     * 删除题库
     * 
     * @param questionId 题库主键
     * @return 结果
     */
    public int deleteCyanQuestionByQuestionId(Long questionId);

    /**
     * 批量删除题库
     * 
     * @param questionIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCyanQuestionByQuestionIds(Long[] questionIds);
}
