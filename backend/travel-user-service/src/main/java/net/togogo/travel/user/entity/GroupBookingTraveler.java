package net.togogo.travel.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_group_booking_traveler")
public class GroupBookingTraveler {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bookingId;
    private Long travelerId;
}