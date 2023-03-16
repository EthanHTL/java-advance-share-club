package club.smileboy.app.common.controller;

import club.smileboy.app.common.aop.AopBasedStudy;
import club.smileboy.app.common.aop.AopTargetBean;
import club.smileboy.app.common.aop.LazyComponent;
import club.smileboy.app.common.aop.commons.candidate.DefaultService;
import club.smileboy.app.common.aop.weave.bean.EntitlementCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("aop")
public class AopController {

    @Autowired
    AopTargetBean aopTarget;

    @Autowired
    AopBasedStudy aopBasedStudy;

//    @Autowired
//    AopTargetBean aopTarget2;

    @Autowired
    EntitlementCalculationService service;

    @Autowired
    DefaultService defaultService;

    @GetMapping
    public void methodInvoke() {
        aopTarget.printf();
    }

    @GetMapping("toto")
    public void methodTotoInvoke() {
        aopTarget.toto();
    }

    @GetMapping("study")
    public void aopBaseStudyInvoke() {

        System.out.println("target class" + aopBasedStudy.getClass());
        aopBasedStudy.printf();
    }
    /**  注入点 + Lazy => provider / supplier / objectFactory     */
    @Lazy
    @Autowired
    private LazyComponent lazyComponent;

    @GetMapping("lazy")
    public void lazy() {
        System.out.println(lazyComponent);
    }

    @GetMapping("entitle")
    public void entitle() throws InterruptedException {
        service.calculateEntitlement();
    }

    @GetMapping("mixin")
    public void mixin() {
        // A ,你觉得它是B??
        defaultService.printf();
    }



}
