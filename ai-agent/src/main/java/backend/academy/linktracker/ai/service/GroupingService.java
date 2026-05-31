package backend.academy.linktracker.ai.service;

import backend.academy.linktracker.avro.LinkUpdateAvro;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupingService {

    private final KafkaProcessedUpdateSender kafkaSender;
    private final UpdateAggregator updateAggregator;

    private final ConcurrentMap<Long, Queue<LinkUpdateAvro>> buffer = new ConcurrentHashMap<>();

    public void addUpdate(LinkUpdateAvro update) {
        for (Long chatId : update.getTgChatIds()) {
            buffer.computeIfAbsent(chatId, k -> new ConcurrentLinkedQueue<>()).add(update);
        }
    }

    public void flushGroupedUpdates() {
        buffer.forEach((chatId, queue) -> {
            if (queue == null || queue.isEmpty()) return;

            List<LinkUpdateAvro> updates = extractAll(queue);

            buffer.computeIfPresent(chatId, (k, q) -> q.isEmpty() ? null : q);

            if (updates.isEmpty()) return;

            log.atInfo()
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("batchSize", updates.size())
                    .log("Flushing grouped updates");

            if (updates.size() == 1) {
                sendSingle(chatId, updates.get(0));
            } else {
                sendGrouped(chatId, updates);
            }
        });
    }

    private List<LinkUpdateAvro> extractAll(Queue<LinkUpdateAvro> queue) {
        List<LinkUpdateAvro> updates = new ArrayList<>();
        LinkUpdateAvro item;
        while ((item = queue.poll()) != null) {
            updates.add(item);
        }
        return updates;
    }

    private void sendSingle(Long chatId, LinkUpdateAvro update) {
        LinkUpdateAvro single =
                LinkUpdateAvro.newBuilder(update).setTgChatIds(List.of(chatId)).build();
        kafkaSender.send(single);
    }

    private void sendGrouped(Long chatId, List<LinkUpdateAvro> updates) {
        LinkUpdateAvro grouped = updateAggregator.aggregate(chatId, updates);
        kafkaSender.send(grouped);
    }
}
