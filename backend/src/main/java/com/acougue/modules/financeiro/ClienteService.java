package com.acougue.modules.financeiro;

import com.acougue.entity.Cliente;
import com.acougue.repository.ClienteRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepo;

    public List<Cliente> listarAtivos() {
        return clienteRepo.findByAtivoTrue();
    }

    public List<Cliente> buscarPorNome(String nome) {
        return clienteRepo.buscarPorNome(nome);
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado: " + id));
    }

    @Transactional
    public Cliente criar(Cliente cliente) {
        cliente.setId(null); // garante criação, nunca sobrescreve por engano
        return clienteRepo.save(cliente);
    }
}