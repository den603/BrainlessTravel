package net.togogo.travel.ai.util;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

//用于后续队伍更新插入新时间
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    // 插入时自动填充
    @Override
    public void insertFill(MetaObject metaObject) {
        // 填充create_time字段
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        // 同时填充update_time（可选）
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

    }

    // 更新时自动填充
    @Override
    public void updateFill(MetaObject metaObject) {
        // 填充update_time字段
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}