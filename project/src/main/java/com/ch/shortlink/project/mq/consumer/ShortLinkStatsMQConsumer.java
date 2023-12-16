package com.ch.shortlink.project.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.ch.shortlink.project.common.constant.RocketMQConstant;
import com.ch.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.ch.shortlink.project.mq.domain.MessageWrapper;
import com.ch.shortlink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @Author hui cao
 * @Description: 延迟访问统计消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = RocketMQConstant.DELAY_STATS_TOPIC_KEY,
        selectorExpression = RocketMQConstant.STATS_TAG_KEY,
        consumerGroup = RocketMQConstant.STATS_CG_KEY
)
public class ShortLinkStatsMQConsumer implements RocketMQListener<MessageWrapper<ShortLinkStatsRecordDTO>> {

    private final ShortLinkService shortLinkService;

    @Override
    public void onMessage(MessageWrapper<ShortLinkStatsRecordDTO> message) {
        log.info("[访问统计Consumer] 开始消费：{}", JSON.toJSONString(message));
        ShortLinkStatsRecordDTO shortLinkStatsRecordDTO = message.getMessage();
        try{
            shortLinkService.shortLinkStats(null,null,shortLinkStatsRecordDTO);
        }catch (Throwable ex){
            log.error("[访问统计Consumer] 短链接：{} 访问统计服务失败", shortLinkStatsRecordDTO.getFullShortUrl(), ex);
            throw ex;
        }

    }
}
