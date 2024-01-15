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
        selectorExpression = RocketMQConstant.STATS_TAG_KEY,
        consumerGroup = RocketMQConstant.STATS_CG_KEY
)
public class ShortLinkStatsMQConsumer implements RocketMQListener<MessageWrapper<ShortLinkStatsRecordDTO>> {

    private final ShortLinkService shortLinkService;
    private final MessageQueueIdempotentHandler messageQueueIdempotentHandler;

    @Override
    public void onMessage(MessageWrapper<ShortLinkStatsRecordDTO> message) {

        String id = message.getUuid();

        // 如果被消费过
        if (!messageQueueIdempotentHandler.isMessageProcessed(id)) {
            // 判断当前的这个消息流程是否执行完成
            if (messageQueueIdempotentHandler.isAccomplish(id)) {
                return;
            }
            throw new ServiceException("消息未完成流程，需要消息队列重试");
        }

        // 没有被消费过
        try {
            log.info("[访问统计Consumer] 开始消费：{}", JSON.toJSONString(message));
            ShortLinkStatsRecordDTO shortLinkStatsRecordDTO = message.getMessage();
            shortLinkService.shortLinkStats(null,null,shortLinkStatsRecordDTO);
        } catch (Throwable ex) {
            // 某情况宕机了
            // 如果消息处理遇到异常情况，删除幂等标识
            messageQueueIdempotentHandler.delMessageProcessed(id);
            log.error("记录短链接监控消费异常", ex);
            throw new ServiceException("消息未完成流程，需要消息队列重试");
        }
        messageQueueIdempotentHandler.setAccomplish(id);
    }
}
