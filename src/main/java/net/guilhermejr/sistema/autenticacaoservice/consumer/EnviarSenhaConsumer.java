package net.guilhermejr.sistema.autenticacaoservice.consumer;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.guilhermejr.sistema.autenticacaoservice.api.dto.EsqueciMinhaSenhaDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Log4j2
@RequiredArgsConstructor
@Service
public class EnviarSenhaConsumer {

    private final AmazonSQS amazonSQSClient;
    private final JavaMailSender emailSender;

    @Value("${cloud.aws.fila.esqueci-minha-senha.url}")
    private String esqueciMinhaSenha;

    @Scheduled(fixedDelayString = "${cloud.aws.fila.esqueci-minha-senha.delay}")
    public void enviarSenha() {

        log.info("Verificando se existe mensagens a serem lidas");

        ReceiveMessageResult result =  amazonSQSClient.receiveMessage(esqueciMinhaSenha);

        if (!result.getMessages().isEmpty()) {

            for (Message mensagem : result.getMessages()) {

                log.info("Processando mensagem: {}", mensagem.getReceiptHandle());

                String json = mensagem.getBody();

                ObjectMapper mapper = new ObjectMapper();
                EsqueciMinhaSenhaDTO esqueciMinhaSenhaDTO = null;
                try {
                    esqueciMinhaSenhaDTO = mapper.readValue(json, EsqueciMinhaSenhaDTO.class);
                    String texto = "Olá " + esqueciMinhaSenhaDTO.getNome() + "\n\nSua nova senha é: " + esqueciMinhaSenhaDTO.getSenha();
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setTo(esqueciMinhaSenhaDTO.getEmail());
                    message.setSubject("Nova senha");
                    message.setText(texto);
                    emailSender.send(message);
                    log.info("E-mail enviado com sucesso para {}", esqueciMinhaSenhaDTO.getEmail());
                } catch (JsonProcessingException e) {
                    log.error("Erro ao converter String em JSON - {}", e.getMessage());
                } catch (MailException e) {
                    log.error("Erro ao enviar e-mail para {} - {}", esqueciMinhaSenhaDTO.getEmail(), e.getMessage());
                }

                amazonSQSClient.deleteMessage(esqueciMinhaSenha, mensagem.getReceiptHandle());
                log.info("Mensagem apagada da fila com sucesso: {}", mensagem.getReceiptHandle());

            }

        } else {

            log.info("Não existe mensagens a serem lidas");

        }

    }

}
