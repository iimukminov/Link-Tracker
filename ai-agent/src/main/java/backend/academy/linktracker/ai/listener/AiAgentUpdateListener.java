package backend.academy.linktracker.ai.listener;

import backend.academy.linktracker.ai.service.AiAgentUpdateProcessor;
import backend.academy.linktracker.ai.service.KafkaProcessedUpdateSender;
import backend.academy.linktracker.avro.LinkUpdateAvro;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiAgentUpdateListener {

    private final AiAgentUpdateProcessor updateProcessor;
    private final KafkaProcessedUpdateSender kafkaSender;

    @KafkaListener(topics = "${app.kafka.topic.raw-updates}")
    public void listen(LinkUpdateAvro rawUpdate) {
        log.atInfo().addKeyValue("updateId", rawUpdate.getId()).log("Received raw update");

        try {
            updateProcessor.process(rawUpdate).ifPresent(kafkaSender::send);

        } catch (Exception e) {
            log.atError().setCause(e).addKeyValue("updateId", rawUpdate.getId()).log("Error processing raw update");
        }
    }
}
