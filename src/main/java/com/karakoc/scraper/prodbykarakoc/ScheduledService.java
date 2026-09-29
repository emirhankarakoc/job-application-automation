package com.karakoc.scraper.prodbykarakoc;

import com.karakoc.scraper.exceptions.general.BadRequestException;
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
public class ScheduledService  {

    private final QueueService karakocService;
    private final OrderRequestsRepository orderRequestsRepository;

@Scheduled(initialDelay = 5000)
    public void calis() {
        while (true) {
            try {
                List<OrderRequest> orders = orderRequestsRepository.findAllByStatus(OrderStatus.PENDING);

                if (orders.isEmpty()) {
                    log.info("📌 Bekleyen sipariş yok, 1 dakika sonra tekrar kontrol edilecek.");
                    Thread.sleep(60000);
                    continue;
                }

                OrderRequest order = orders.get(0);
                log.info("🔄 Yeni sipariş işleniyor: " + order.getLinkedinUrl());

                try {
                    karakocService.run(order.getId());
                    order.setStatus(OrderStatus.DONE);
                    orderRequestsRepository.save(order);
                    log.info("✅ İşlem başarıyla tamamlandı!");
                    karakocService.sendOrderDetailsToUser(order);
                } catch (BadRequestException e) {
                    if ("Bir tane mail bile bulunamadi.".equals(e.getMessage())) {
                        order.setStatus(OrderStatus.FAILED);
                        orderRequestsRepository.save(order);
                        karakocService.sendOrderFailedToUser(order,"📌 Bir tane iş ilanında mail bulunamadı.");
                    }
                } catch (Exception e) {
                    log.error("❌ Hata meydana geldi: {}", e.getMessage(), e);
                }

                log.info("📌 İşlem tamamlandı, yeni sipariş bekleniyor...");
            } catch (Exception e) {
                log.error("🔥 Kritik hata: {}", e.getMessage(), e);
            }
        }
    }
}
