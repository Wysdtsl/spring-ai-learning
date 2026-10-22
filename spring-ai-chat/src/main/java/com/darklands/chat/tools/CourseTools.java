package com.darklands.chat.tools;

import com.darklands.chat.entity.po.Course;
import com.darklands.chat.entity.query.CourseQuery;
import com.darklands.chat.service.ICourseReservationService;
import com.darklands.chat.service.ICourseService;
import com.darklands.chat.service.ISchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.objenesis.instantiator.perc.PercInstantiator;
import org.springframework.stereotype.Component;

import java.util.List;
@RequiredArgsConstructor
@Component
public class CourseTools {

    private final ICourseService courseService;
    private final ISchoolService schoolService;
    private final ICourseReservationService courseReservationService;

    @Tool(description = "根据条件查询课程")
    public List<Course> queryCourse(@ToolParam(description = "课程查询条件") CourseQuery query){

return null;
    }
}
