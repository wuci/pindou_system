package com.pindou.timer.controller;

import org.springframework.web.bind.annotation.RequestMapping;

/**
 * API 控制器基类
 * <p>
 * 所有 API 控制器继承此类后，会自动添加 /api 路径前缀。
 * 这样可以保持 URL 结构清晰，便于前端调用和 API 管理。
 * </p>
 *
 * @author wuci
 * @date 2026-04-05
 */
@RequestMapping("/api")
public abstract class ETSBaseController {
    // API 控制器基类，无需额外实现
}
