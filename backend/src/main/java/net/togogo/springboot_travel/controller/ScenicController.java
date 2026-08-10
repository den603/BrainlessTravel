package net.togogo.springboot_travel.controller;

import jakarta.annotation.Resource;
import net.togogo.springboot_travel.Result.Result;
import net.togogo.springboot_travel.entity.Scenic;
import net.togogo.springboot_travel.service.ScenicService;
import net.togogo.springboot_travel.util.MinioUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 景点管理 Controller（REST API 接口层）
 *
 * 【职责说明】
 * 接收前端 HTTP 请求，调用 Service 层处理业务逻辑，返回统一格式的 Result 对象。
 * 不直接操作数据库，所有数据操作委托给 Service。
 *
 * 【接口路径规则】
 * application.yml 中配置了 context-path: /api
 * 本类配置了 @RequestMapping("/scenic")
 * 所以完整接口地址为：http://localhost:8080/api/scenic/xxx
 */
@RestController              // 声明为 REST 控制器，所有方法返回 JSON 数据（不是 HTML 页面）
@RequestMapping("/scenic")   // 该控制器下所有接口的 URL 前缀
public class ScenicController {

    /**
     * 注入景点 Service（面向接口编程，降低耦合）
     */
    @Resource
    private ScenicService scenicService;

    @Resource
    private MinioUtil minioUtil;
    /**
     * 获取全部景点列表
     *
     * 【请求】GET http://localhost:8080/api/scenic/list
     * 【缓存】实际调用 getListWithCache()，内部自动走 Redis 缓存
     * 【响应】{ "code": 1, "data": [ {...}, {...} ], "msg": "" }
     *
     * @return Result 统一响应对象，code=1 表示成功
     */
    @GetMapping("/list")
    public Result<List<Scenic>> getList() {
        // 调用带缓存的 Service 方法
        List<Scenic> list = scenicService.getListWithCache();
        // 用 Result.success() 包装，前端统一解析格式
        return Result.success(list);
    }

    /**
     * 根据 ID 获取景点详情
     *
     * 【请求】GET http://localhost:8080/api/scenic/detail/{id}
     * 【示例】GET http://localhost:8080/api/scenic/detail/1
     * 【缓存】实际调用 getDetailWithCache()，内部自动走 Redis 缓存
     *
     * @param id 景点 ID，从 URL 路径变量中获取（@PathVariable）
     * @return 景点详情；找不到返回失败信息
     */
    @GetMapping("/detail/{id}")
    public Result<Scenic> getDetail(@PathVariable Integer id) {
        // 调用带缓存的 Service 方法
        Scenic scenic = scenicService.getDetailWithCache(id);

        // 数据不存在，返回标准失败响应
        if (scenic == null) {
            return Result.fail("景点不存在或已被删除");
        }

        return Result.success(scenic);
    }

    /**
     * 新增景点
     *
     * 【请求】POST http://localhost:8080/api/scenic/add
     * 【请求体】JSON 格式的 Scenic 对象
     * 【Content-Type】application/json
     *
     * 【缓存处理】
     * 新增成功后，原来的景点列表缓存已经不全了（少了这条新数据），
     * 必须删除缓存，下次请求会自动从数据库加载最新完整列表。
     *
     * @param scenic 前端传来的景点 JSON，Spring 自动映射为 Scenic 实体对象（@RequestBody）
     * @return 新增结果
     */
    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody Scenic scenic) {
        // 调用 MyBatis-Plus 的 save 方法插入数据库
        boolean saved = scenicService.save(scenic);

        if (saved) {
            // 【关键】数据库写入成功，立即删除 Redis 缓存，保证数据一致性
            scenicService.clearScenicCache();
        }

        return saved ? Result.success(true) : Result.fail("新增景点失败");
    }

    /**
     * 修改景点
     *
     * 【请求】PUT http://localhost:8080/api/scenic/update
     * 【请求体】JSON 格式的 Scenic 对象（必须包含 id 字段）
     *
     * @param scenic 要修改的景点信息
     * @return 修改结果
     */
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody Scenic scenic) {
        boolean updated = scenicService.updateById(scenic);

        if (updated) {
            // 修改成功后清除缓存，保证下次读到最新数据
            scenicService.clearScenicCache();
        }

        return updated ? Result.success(true) : Result.fail("修改景点失败");
    }

    /**
     * 删除景点（同步删除 MinIO 图片）
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Integer id) {
        Scenic scenic = scenicService.getById(id);
        if (scenic != null && scenic.getImg() != null) {
            // 提取 objectName 并删除
            String objectName = minioUtil.extractObjectName(scenic.getImg());
            if (objectName != null) {
                try {
                    minioUtil.delete(objectName);
                    System.out.println(">>> 【MinIO】图片已删除，objectName=" + objectName);
                } catch (Exception e) {
                    System.err.println(">>> 【MinIO】图片删除失败：" + e.getMessage());
                }
            }
        }
        boolean removed = scenicService.removeById(id);
        if (removed) {
            scenicService.clearScenicCache();
        }
        return removed ? Result.success(true) : Result.fail("删除失败");
    }
}