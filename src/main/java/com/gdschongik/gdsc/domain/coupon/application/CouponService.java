package com.gdschongik.gdsc.domain.coupon.application;

import static com.gdschongik.gdsc.domain.coupon.domain.CouponType.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.common.vo.Money;
import com.gdschongik.gdsc.domain.coupon.dao.CouponRepository;
import com.gdschongik.gdsc.domain.coupon.dao.IssuedCouponRepository;
import com.gdschongik.gdsc.domain.coupon.domain.Coupon;
import com.gdschongik.gdsc.domain.coupon.domain.CouponType;
import com.gdschongik.gdsc.domain.coupon.domain.IssuedCoupon;
import com.gdschongik.gdsc.domain.coupon.dto.request.CouponCreateRequest;
import com.gdschongik.gdsc.domain.coupon.dto.request.CouponIssueRequest;
import com.gdschongik.gdsc.domain.coupon.dto.request.IssuedCouponQueryOption;
import com.gdschongik.gdsc.domain.coupon.dto.response.CouponResponse;
import com.gdschongik.gdsc.domain.coupon.dto.response.CouponTypeResponse;
import com.gdschongik.gdsc.domain.coupon.dto.response.IssuedCouponResponse;
import com.gdschongik.gdsc.domain.coupon.util.CouponNameUtil;
import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.study.dao.StudyHistoryRepository;
import com.gdschongik.gdsc.domain.study.dao.StudyRepository;
import com.gdschongik.gdsc.domain.study.domain.Study;
import com.gdschongik.gdsc.domain.study.domain.StudyHistory;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.util.MemberUtil;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

    private final CouponNameUtil couponNameUtil;
    private final MemberUtil memberUtil;
    private final StudyHistoryRepository studyHistoryRepository;
    private final StudyRepository studyRepository;
    private final CouponRepository couponRepository;
    private final IssuedCouponRepository issuedCouponRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void createCoupon(CouponCreateRequest request) {
        Optional<Study> study = Optional.ofNullable(request.studyId()).flatMap(studyRepository::findById);
        Coupon coupon = Coupon.createManual(
                request.name(), Money.from(request.discountAmount()), request.couponType(), study.orElse(null));
        couponRepository.save(coupon);
        log.info("[CouponService] 쿠폰 생성: name={}, discountAmount={}", request.name(), request.discountAmount());
    }

    public List<CouponResponse> findAllCoupons() {
        return couponRepository.findAll().stream().map(CouponResponse::from).toList();
    }

    public Page<IssuedCouponResponse> findAllIssuedCoupons(IssuedCouponQueryOption queryOption, Pageable pageable) {
        Page<IssuedCoupon> issuedCoupons = issuedCouponRepository.findAllIssuedCoupons(queryOption, pageable);
        return issuedCoupons.map(IssuedCouponResponse::from);
    }

    @Transactional
    public void createIssuedCoupon(CouponIssueRequest request) {
        Coupon coupon =
                couponRepository.findById(request.couponId()).orElseThrow(() -> new CustomException(COUPON_NOT_FOUND));

        List<Member> members = memberRepository.findAllById(request.memberIds());

        List<IssuedCoupon> issuedCoupons = members.stream()
                .map(member -> IssuedCoupon.create(coupon, member))
                .toList();

        issuedCouponRepository.saveAll(issuedCoupons);

        log.info(
                "[CouponService] 쿠폰 발급: issuedCouponIds={}",
                issuedCoupons.stream().map(IssuedCoupon::getId).toList());
    }

    @Transactional
    public void revokeIssuedCoupon(Long issuedCouponId) {
        IssuedCoupon issuedCoupon = issuedCouponRepository
                .findById(issuedCouponId)
                .orElseThrow(() -> new CustomException(ISSUED_COUPON_NOT_FOUND));

        issuedCoupon.revoke();
        log.info("[CouponService] 쿠폰 회수: issuedCouponId={}", issuedCouponId);
    }

    public List<IssuedCouponResponse> findMyUsableIssuedCoupons() {
        Member currentMember = memberUtil.getCurrentMember();

        return issuedCouponRepository.findByMember(currentMember).stream()
                .filter(IssuedCoupon::isUsable)
                .map(IssuedCouponResponse::from)
                .toList();
    }

    @Transactional
    public void createAndIssueCouponByStudyHistories(List<Long> studyHistoryIds) {
        List<StudyHistory> studyHistories = studyHistoryRepository.findAllById(studyHistoryIds);
        List<Long> studentIds = studyHistories.stream()
                .map(studyHistory -> studyHistory.getStudent().getId())
                .toList();
        List<Member> students = memberRepository.findAllById(studentIds);
        Study study = studyHistories.get(0).getStudy();

        Coupon coupon = findOrCreate(STUDY_COMPLETION, study);

        List<IssuedCoupon> issuedCoupons = students.stream()
                .map(student -> IssuedCoupon.create(coupon, student))
                .toList();
        issuedCouponRepository.saveAll(issuedCoupons);

        log.info(
                "[CouponService] 스터디 수료 쿠폰 발급: issuedCouponIds={}",
                issuedCoupons.stream().map(IssuedCoupon::getId).toList());
    }

    private Coupon findOrCreate(CouponType couponType, Study study) {
        return couponRepository.findByCouponTypeAndStudy(couponType, study).orElseGet(() -> {
            String couponName = couponNameUtil.generateStudyCompletionCouponName(study);
            Coupon coupon = Coupon.createAutomatic(couponName, Money.FIVE_THOUSAND, couponType, study);
            return couponRepository.save(coupon);
        });
    }

    @Transactional
    public void revokeStudyCompletionCouponByStudyHistoryId(long studyHistoryId) {
        StudyHistory studyHistory = studyHistoryRepository
                .findById(studyHistoryId)
                .orElseThrow(() -> new CustomException(STUDY_HISTORY_NOT_FOUND));

        IssuedCoupon issuedCoupon = issuedCouponRepository
                .findUnrevokedIssuedStudyCoupon(STUDY_COMPLETION, studyHistory.getStudent(), studyHistory.getStudy())
                .orElseThrow(() -> new CustomException(ISSUED_COUPON_NOT_FOUND));

        issuedCoupon.revoke();
        issuedCouponRepository.save(issuedCoupon);

        log.info("[CouponService] 스터디 수료 쿠폰 회수: issuedCouponId={}", issuedCoupon.getId());
    }

    public List<CouponTypeResponse> getCouponTypes() {
        return Arrays.stream(CouponType.values()).map(CouponTypeResponse::from).toList();
    }
}
