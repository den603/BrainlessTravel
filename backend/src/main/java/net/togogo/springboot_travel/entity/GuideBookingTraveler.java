package net.togogo.springboot_travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_guide_booking_traveler")
public class GuideBookingTraveler {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bookingId;
    private Long travelerId;
}