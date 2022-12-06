package club.smileboy.app.common.controller;

import club.smileboy.app.common.aop.AopBasedStudy;
import club.smileboy.app.common.aop.AopTargetBean;
import club.smileboy.app.common.aop.AopTargetBean1;
import club.smileboy.app.common.aop.LazyComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("aop")
public class AopController {

    @Autowired
    AopTargetBean aopTarget;

    @Autowired
    AopBasedStudy aopBasedStudy;

//    @Autowired
//    AopTargetBean aopTarget2;

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
}
