package net.guilhermejr.sistema.autenticacaoservice.domain.repository;

import net.guilhermejr.sistema.autenticacaoservice.domain.entity.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, UUID> {

    Optional<Perfil> findByDescricao(String descricao);

}
