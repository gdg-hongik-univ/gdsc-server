package com.gdschongik.gdsc.domain.study.domain;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.common.vo.Semester;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.annotation.DomainFactory;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.stream.IntStream;

@DomainFactory
public class StudyFactory {

    /**
     * 스터디 및 스터디회차를 생성합니다.
     * 스터디회차의 경우 총 회차 수만큼 생성되며, 생성 순서에 따라 position 값이 지정됩니다.
     */
    public Study create(
            StudyType type,
            String title,
            Semester semester,
            Integer totalRound,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            Period applicationPeriod,
            String discordChannelId,
            String discordRoleId,
            Member mentor,
            AttendanceNumberGenerator attendanceNumberGenerator,
            Integer minAssignmentLength) {
        if (type.isLive()) {
            return createLive(
                    type,
                    title,
                    semester,
                    totalRound,
                    dayOfWeek,
                    startTime,
                    endTime,
                    applicationPeriod,
                    discordChannelId,
                    discordRoleId,
                    mentor,
                    attendanceNumberGenerator,
                    minAssignmentLength);
        } else {
            return createAssignment(
                    title,
                    semester,
                    totalRound,
                    applicationPeriod,
                    discordChannelId,
                    discordRoleId,
                    mentor,
                    minAssignmentLength);
        }
    }

    private Study createLive(
            StudyType type,
            String title,
            Semester semester,
            Integer totalRound,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            Period applicationPeriod,
            String discordChannelId,
            String discordRoleId,
            Member mentor,
            AttendanceNumberGenerator attendanceNumberGenerator,
            Integer minAssignmentLength) {
        Study study = Study.createLive(
                type,
                title,
                semester,
                totalRound,
                dayOfWeek,
                startTime,
                endTime,
                applicationPeriod,
                discordChannelId,
                discordRoleId,
                mentor,
                minAssignmentLength);

        IntStream.rangeClosed(1, totalRound)
                .forEach(round -> StudySession.createEmptyForLive(round, attendanceNumberGenerator.generate(), study));

        return study;
    }

    private Study createAssignment(
            String title,
            Semester semester,
            Integer totalRound,
            Period applicationPeriod,
            String discordChannelId,
            String discordRoleId,
            Member mentor,
            Integer minAssignmentLength) {
        Study study = Study.createAssignment(
                title,
                semester,
                totalRound,
                applicationPeriod,
                discordChannelId,
                discordRoleId,
                mentor,
                minAssignmentLength);

        IntStream.rangeClosed(1, totalRound).forEach(round -> StudySession.createEmptyForAssignment(round, study));

        return study;
    }
}
