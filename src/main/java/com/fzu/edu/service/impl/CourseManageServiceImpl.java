package com.fzu.edu.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.fei.common.CDataSet;
import com.fzu.edu.dao.CourseArrangeMapper;
import com.fzu.edu.dao.CourseFullArrangeMapper;
import com.fzu.edu.dao.CourseFullInfoMapper;
import com.fzu.edu.dao.CourseManageMapper;
import com.fzu.edu.model.*;
import com.fzu.edu.service.CourseManageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by CLY on 2018/3/22.
 */
@Service("courseManageService")
@Transactional(rollbackFor = Exception.class)
public class CourseManageServiceImpl extends ServiceImpl<CourseManageMapper, CourseInfo> implements CourseManageService {

    @Resource
    private CourseManageMapper courseManageMapper;
    @Resource
    private CourseFullInfoMapper courseFullInfoMapper;
    @Resource
    private CourseArrangeMapper courseArrangeMapper;
    @Resource
    private CourseFullArrangeMapper courseFullArrangeMapper;

    public int addOrUpdateCourse(String params) {
        CourseInfo courseInfo = JSONObject.parseObject(params, CourseInfo.class);
        if (courseInfo.getId() == null) return courseManageMapper.insert(courseInfo);
        else return courseManageMapper.updateById(courseInfo);
    }

    public int addOrUpdateCourseArrage(String params) {
        CourseArrange courseArrange = JSONObject.parseObject(params, CourseArrange.class);
        removeSameWeekAndClassIndex(courseArrange);

        if (checkTeacherExist(courseArrange)){
            return 0;
        }

        if (checkTimeConflict(courseArrange)){
            return 0;
        }

        if (courseArrange.getId() == null) {
            return courseArrangeMapper.insert(courseArrange);
        } else {
            return courseArrangeMapper.updateById(courseArrange);
        }
    }

    public List getAll(Map params) {
        List<CourseFullInfo> course = courseManageMapper.getAllCourse(params);
        return course;
    }

    public List getAllCourseArrage(Map params) {
        List<CourseArrangeDetail> courseArrangeDetails = courseArrangeMapper.getAllCourseArrage();
        for (CourseArrangeDetail courseArrangeDetail : courseArrangeDetails){
            courseArrangeDetail.createYearTerm();
            courseArrangeDetail.createWhenWhere();
        }
        return courseArrangeDetails;
    }

    public List getCollegeAndCourse(Map params) {
        CollegeInfo collegeInfo = CDataSet.MapToData(params, CollegeInfo.class);
        List dt = courseManageMapper.getCollegeAndCourse(collegeInfo);
        List<String> fields = CDataSet.getDeclaredFieldsStrings(CollegeInfo.class);
        String[] fieldsS = fields.toArray(new String[0]);
        List data = CDataSet.MergeField(dt, fieldsS);
        return data;
    }

    /**
     * 去掉重复的上课时间
     * @param courseArrange
     */
    private void removeSameWeekAndClassIndex(CourseArrange courseArrange){
        ArrayList classIndex = JSON.parseObject(courseArrange.getClassIndex(), ArrayList.class);
        ArrayList week = JSON.parseObject(courseArrange.getWeek(), ArrayList.class);
        HashMap W = new HashMap();
        for (int i = 0; i < week.size(); i++){
            Object w = week.get(i);
            Object c = classIndex.get(i);
            if (W.get(w) == null) {
                W.put(w, new HashMap());
            }
            HashMap C = (HashMap) W.get(w);
            for (Object cc : (JSONArray) c){
                C.put(cc, 1);
            }
        }
        ArrayList ci = new ArrayList();
        ArrayList w = new ArrayList();
        for (Object o : W.entrySet()){
            Map.Entry e = (Map.Entry) o;
            Object key = e.getKey();
            HashMap value = (HashMap) e.getValue();
            w.add(key);
            ArrayList cii = new ArrayList();
            for (Object oo : value.entrySet()){
                cii.add(((Map.Entry) oo).getKey());
            }
            ci.add(cii);
        }
        courseArrange.setWeek(JSON.toJSONString(w));
        courseArrange.setClassIndex(JSON.toJSONString(ci));
    }


    /**
     * 检查教师冲突
     * @param courseArrange
     * @return
     */
    private boolean checkTeacherExist(CourseArrange courseArrange) {
        int n = courseArrangeMapper.checkTeacherExist(courseArrange);
        if (n > 0) return true;
        else return false;
    }

    /**
     * 检查时间冲突
     * @param courseArrange
     * @return
     */
    private boolean checkTimeConflict(CourseArrange courseArrange) {
        int n = courseArrangeMapper.checkTimeConflict(courseArrange);
        if (n > 0) return true;
        else return false;
    }
}
