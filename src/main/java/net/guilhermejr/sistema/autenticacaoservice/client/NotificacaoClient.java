package net.guilhermejr.sistema.autenticacaoservice.client;

import net.guilhermejr.sistema.autenticacaoservice.api.dto.EsqueciMinhaSenhaDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notificacao-service", url = "http://${sistema.notificacao.host}/notificacao-service/autenticacao")
public interface NotificacaoClient {

//    @PostMapping("/enviar-recuperar-senha-fila")
//    public ResponseEntity<Void> enviarRecuperarSenhaFila(@RequestBody EsqueciMinhaSenhaDTO esqueciMinhaSenhaDTO);

    @PostMapping("/enviar-link")
    public ResponseEntity<Void> enviarLink(@RequestBody EsqueciMinhaSenhaDTO esqueciMinhaSenhaDTO);

}
