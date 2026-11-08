package com.sky.mapper;

import com.sky.entity.OrderDetail;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    /**
     * 插入订单明细数据
     * @param orderDetailList
     */
    void insetBatch(List<OrderDetail> orderDetailList);
}
