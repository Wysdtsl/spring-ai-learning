package com.darklands.chat.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.darklands.chat.entity.po.Course;
import com.darklands.chat.entity.po.CourseReservation;
import com.darklands.chat.entity.po.School;
import com.darklands.chat.service.ICourseReservationService;
import com.darklands.chat.service.ICourseService;
import com.darklands.chat.service.ISchoolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 智能客服可调用的工具。
 *
 * 模型只负责决定“调用哪个方法、传什么参数”，真正的查询和写库都在 Java 里完成。
 * 所有参数都要校验，不能把模型输出直接拼进 SQL。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CourseTools {

    private static final int MAX_RESULTS = 10;
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ICourseService courseService;
    private final ISchoolService schoolService;
    private final ICourseReservationService courseReservationService;

    @Tool(description = """
            根据课程方向、学历要求、关键词和价格区间查询课程。
            返回课程 ID、名称、方向、学历要求、学习周期、价格和简介。
            用户问有哪些课程、课程价格、学多久、适合什么学历时使用。
            """)
    public List<CourseInfo> queryCourse(
            @ToolParam(required = false, description = "课程方向，例如 Java、Python、前端、测试、设计、AI；不传表示不限")
            String type,
            @ToolParam(required = false, description = "学历要求，例如 不限、大专及以上、本科及以上；不传表示不限")
            String education,
            @ToolParam(required = false, description = "课程名称或简介关键词，例如 Java、大数据、Vue；不传表示不限")
            String keyword,
            @ToolParam(required = false, description = "最低价格，单位元；不传表示不限")
            BigDecimal minPrice,
            @ToolParam(required = false, description = "最高价格，单位元；不传表示不限")
            BigDecimal maxPrice) {

        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .eq(StringUtils.hasText(type), Course::getType, type)
                .eq(StringUtils.hasText(education), Course::getEducation, education)
                .ge(minPrice != null, Course::getPrice, minPrice)
                .le(maxPrice != null, Course::getPrice, maxPrice);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Course::getName, keyword)
                    .or()
                    .like(Course::getDescription, keyword));
        }

        wrapper.orderByAsc(Course::getPrice).last("LIMIT " + MAX_RESULTS);

        List<CourseInfo> result = courseService.list(wrapper).stream()
                .map(CourseInfo::from)
                .toList();

        log.debug("queryCourse type={}, education={}, keyword={}, minPrice={}, maxPrice={}, result={}",
                type, education, keyword, minPrice, maxPrice, result.size());
        return result;
    }

    @Tool(description = """
            根据城市或校区名称查询校区。
            返回校区 ID、名称、城市、地址和咨询电话。
            用户问在哪里上课、有哪些校区、校区地址或电话时使用。
            """)
    public List<SchoolInfo> querySchool(
            @ToolParam(required = false, description = "城市，例如 北京、上海、广州、深圳、杭州、成都；不传表示不限")
            String city,
            @ToolParam(required = false, description = "校区名称关键词；不传表示不限")
            String keyword) {

        LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<School>()
                .like(StringUtils.hasText(city), School::getCity, city);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(School::getName, keyword)
                    .or()
                    .like(School::getAddress, keyword));
        }

        wrapper.orderByAsc(School::getCity).last("LIMIT " + MAX_RESULTS);

        List<SchoolInfo> result = schoolService.list(wrapper).stream()
                .map(SchoolInfo::from)
                .toList();

        log.debug("querySchool city={}, keyword={}, result={}", city, keyword, result.size());
        return result;
    }

    @Tool(description = """
            根据手机号查询预约记录。
            返回预约 ID、课程名称、校区名称、状态、备注和创建时间。
            用户问自己的预约、报名进度时使用。
            """)
    public List<ReservationInfo> queryReservation(
            @ToolParam(description = "预约人手机号，11 位") String phone) {
                                    //trim() 的作用是：去掉字符串开头和结尾的空白字符，返回一个新字符串。
        String normalizedPhone = phone == null ? null : phone.trim();
        if (!isValidPhone(normalizedPhone)) {
            return List.of();
        }

        List<CourseReservation> reservations = courseReservationService.list(
                new LambdaQueryWrapper<CourseReservation>()
                        .eq(CourseReservation::getPhone, normalizedPhone)
                        .orderByDesc(CourseReservation::getCreatedAt)
                        .last("LIMIT " + MAX_RESULTS));

        if (reservations.isEmpty()) {
            return List.of();
        }

        Map<Long, Course> courseMap = courseService.listByIds(
                        reservations.stream()
                                .map(CourseReservation::getCourseId)
                                .distinct()
                                .toList())
                .stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));

        Map<Long, School> schoolMap = schoolService.listByIds(
                        reservations.stream()
                                .map(CourseReservation::getSchoolId)
                                .distinct()
                                .toList())
                .stream()
                .collect(Collectors.toMap(School::getId, Function.identity()));

        return reservations.stream().map(reservation -> {
            Course course = courseMap.get(reservation.getCourseId());
            School school = schoolMap.get(reservation.getSchoolId());
            return new ReservationInfo(
                    reservation.getId(),
                    reservation.getUserName(),
                    maskPhone(reservation.getPhone()),
                    course == null ? "未知课程" : course.getName(),
                    school == null ? "未知校区" : school.getName(),
                    reservation.getStatus(),
                    reservation.getRemark(),
                    reservation.getCreatedAt() == null
                            ? ""
                            : reservation.getCreatedAt().format(DATE_TIME_FORMATTER));
        }).toList();
    }

    @Tool(description = """
            创建课程预约。
            调用前必须确认用户姓名、11 位手机号、课程 ID 和校区 ID；
            课程 ID 和校区 ID 必须来自 queryCourse、querySchool 的返回结果，不能编造。
            """)
    public ReservationResult createReservation(
            @ToolParam(description = "预约人姓名") String userName,
            @ToolParam(description = "预约人手机号，11 位") String phone,
            @ToolParam(description = "课程 ID，必须来自 queryCourse 的返回结果") Long courseId,
            @ToolParam(description = "校区 ID，必须来自 querySchool 的返回结果") Long schoolId,
            @ToolParam(required = false, description = "备注，例如想咨询的时间；没有就不传") String remark) {

        if (!StringUtils.hasText(userName) || userName.length() > 50) {
            return ReservationResult.fail("预约人姓名不能为空，且不能超过 50 个字符。");
        }

        String normalizedPhone = phone == null ? null : phone.trim();
        if (!isValidPhone(normalizedPhone)) {
            return ReservationResult.fail("手机号格式不正确，请提供 11 位中国大陆手机号。");
        }
        if (courseId == null || schoolId == null) {
            return ReservationResult.fail("缺少课程或校区，请先查询课程和校区。");
        }

        String safeRemark = remark == null ? "" : remark.trim();
        if (safeRemark.length() > 500) {
            return ReservationResult.fail("备注不能超过 500 个字符。");
        }

        Course course = courseService.getById(courseId);
        if (course == null) {
            return ReservationResult.fail("课程不存在，请重新查询课程。");
        }

        School school = schoolService.getById(schoolId);
        if (school == null) {
            return ReservationResult.fail("校区不存在，请重新查询校区。");
        }

        CourseReservation existing = courseReservationService.getOne(
                new LambdaQueryWrapper<CourseReservation>()
                        .eq(CourseReservation::getPhone, normalizedPhone)
                        .eq(CourseReservation::getCourseId, courseId)
                        .eq(CourseReservation::getSchoolId, schoolId)
                        .eq(CourseReservation::getStatus, "PENDING")
                        .last("LIMIT 1"));

        if (existing != null) {
            return ReservationResult.ok(existing.getId(),
                    "你已经在 " + school.getName() + " 预约过 " + course.getName()
                            + "，预约编号是 " + existing.getId() + "，请勿重复提交。");
        }

        CourseReservation reservation = new CourseReservation()
                .setUserName(userName.trim())
                .setPhone(normalizedPhone)
                .setCourseId(courseId)
                .setSchoolId(schoolId)
                .setStatus("PENDING")
                .setRemark(safeRemark);

        courseReservationService.save(reservation);
        log.info("创建预约成功 id={}, phone={}, courseId={}, schoolId={}",
                reservation.getId(), maskPhone(normalizedPhone), courseId, schoolId);

        return ReservationResult.ok(reservation.getId(),
                "预约成功，预约编号是 " + reservation.getId()
                        + "。课程：" + course.getName()
                        + "，校区：" + school.getName() + "。");
    }

    private static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^1[3-9]\\d{9}$");
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    public record CourseInfo(Long id,
                             String name,
                             String type,
                             String education,
                             String duration,
                             BigDecimal price,
                             String description) {

        static CourseInfo from(Course course) {
            return new CourseInfo(
                    course.getId(),
                    course.getName(),
                    course.getType(),
                    course.getEducation(),
                    course.getDuration(),
                    course.getPrice(),
                    course.getDescription());
        }
    }

    public record SchoolInfo(Long id,
                             String name,
                             String city,
                             String address,
                             String phone) {

        static SchoolInfo from(School school) {
            return new SchoolInfo(
                    school.getId(),
                    school.getName(),
                    school.getCity(),
                    school.getAddress(),
                    school.getPhone());
        }
    }

    public record ReservationInfo(Long id,
                                  String userName,
                                  String phone,
                                  String courseName,
                                  String schoolName,
                                  String status,
                                  String remark,
                                  String createdAt) {
    }

    public record ReservationResult(boolean success, Long reservationId, String message) {

        static ReservationResult ok(Long reservationId, String message) {
            return new ReservationResult(true, reservationId, message);
        }

        static ReservationResult fail(String message) {
            return new ReservationResult(false, null, message);
        }
    }
}
