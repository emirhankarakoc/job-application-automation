package com.karakoc.scraper.prodbykarakoc;

import com.karakoc.scraper.orderrequests.OrderRequest;
import com.karakoc.scraper.orderrequests.OrderRequestsRepository;
import com.karakoc.scraper.orderrequests.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ScheduledService {

    private final QueueService queueService;
    private final OrderRequestsRepository orderRequestsRepository;

    // One order per poll. The worker is local to this application process.
    @Scheduled(initialDelay = 5000, fixedDelay = 60000)
    public void processNextOrder() {
        List<OrderRequest> pending = orderRequestsRepository.findAllByStatus(OrderStatus.PENDING);
        if (pending.isEmpty()) {
            return;
        }

        OrderRequest order = pending.get(0);
        try {
            queueService.run(order.getId());
            order.setStatus(OrderStatus.DONE);
            orderRequestsRepository.save(order);

            try {
                queueService.sendOrderDetailsToUser(order);
            } catch (Exception notificationError) {
                log.warn("Order {} completed, but the notification failed", order.getId(), notificationError);
            }
        } catch (Exception processingError) {
            order.setStatus(OrderStatus.FAILED);
            orderRequestsRepository.save(order);
            log.error("Order {} failed", order.getId(), processingError);

            try {
                queueService.sendOrderFailedToUser(order, "Job application request failed.");
            } catch (Exception notificationError) {
                log.warn("Could not send failure notice for order {}", order.getId(), notificationError);
            }
        }
    }
}
