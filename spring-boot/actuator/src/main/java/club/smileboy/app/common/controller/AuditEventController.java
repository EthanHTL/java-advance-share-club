package club.smileboy.app.common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 自定义 audit 事件派发 ...
 *
 * 本质上audit 事件就是一种审查的功能,也可以理解为日志,那么这个日志中存储的内容可以是跟业务相关的 ..
 *
 * 例如用来存储用户的行为信息,然后进行简单分析,展示，处理 ..
 */
@RestController
@RequestMapping("audit/event")
public class AuditEventController {

    @Autowired
    private AuditEventRepository auditEventRepository;

    @GetMapping("trigger")
    public void trigger() {
        auditEventRepository.add(new AuditEvent("利好","123123","424234234234"));
    }
}
