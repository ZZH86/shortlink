package com.ch.shortlink.project.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.ch.shortlink.project.common.constant.RocketMQConstant;
import com.ch.shortlink.project.common.convention.exception.ServiceException;
import com.ch.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.ch.shortlink.project.mq.domain.MessageWrapper;
import com.ch.shortlink.project.mq.idempotent.MessageQueueIdempotentHandler;
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
        selectorExpression = RocketMQConstant.DELAY_STATS_TAG_KEY,
        consumerGroup = RocketMQConstant.DELAY_STATS_CG_KEY
)
public class DelayShortLinkStatsMQConsumer implements RocketMQListener<MessageWrapper<ShortLinkStatsRecordDTO>> {

    private final ShortLinkService shortLinkService;
    private final MessageQueueIdempotentHandler messageQueueIdempotentHandler;

    @Override
    public void onMessage(MessageWrapper<ShortLinkStatsRecordDTO> message) {
        // 获取唯一标识
        String uuid = message.getUuid();

        // 消息被消费
        if(!messageQueueIdempotentHandler.isMessageProcessed(uuid)){
            if(messageQueueIdempotentHandler.isAccomplish(uuid)){
                return;
            }
            throw new ServiceException("消息未消费完成，需要消息队列重试");
        }

        // 消息未被消费
        ShortLinkStatsRecordDTO shortLinkStatsRecordDTO = message.getMessage();
        try{
            log.info("[延迟访问统计Consumer] 开始消费：{}", JSON.toJSONString(message));
            shortLinkService.shortLinkStats(null,null,shortLinkStatsRecordDTO);
        }catch (Throwable ex){
            log.error("[延迟访问统计Consumer] 短链接：{} 访问统计服务失败", shortLinkStatsRecordDTO.getFullShortUrl(), ex);
            messageQueueIdempotentHandler.delMessageProcessed(uuid);
        }
        messageQueueIdempotentHandler.setAccomplish(uuid);

    }
}
