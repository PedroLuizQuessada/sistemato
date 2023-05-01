package com.quesssystems.sistemato.beans.token;

import com.quesssystems.sistemato.exceptions.TokenNaoEncontradoException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TokenService {
    private final TokenRepository tokenRepository;

    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public List<Token> listAll(boolean ativo) {
        List<Token> tokens = (List<Token>) tokenRepository.findAll();
        tokens.removeIf(token -> token.isAtivo() != ativo);
        return tokens;
    }

    public Token get(Integer id) throws TokenNaoEncontradoException {
        Optional<Token> tokenOptional = tokenRepository.findById(id);
        if (tokenOptional.isPresent()) {
            return tokenOptional.get();
        }
        else {
            throw new TokenNaoEncontradoException(id);
        }
    }

    public boolean ativoToggle(Integer id) throws TokenNaoEncontradoException {
        Token token = get(id);
        token.setAtivo(!token.isAtivo());
        tokenRepository.save(token);
        return token.isAtivo();
    }

    public void delete(Integer id) throws TokenNaoEncontradoException {
        get(id);
        tokenRepository.deleteById(id);
    }

    public void save(Token token) {
        tokenRepository.save(token);
    }

    public boolean isCodigoValido(String codigo) {
        Token token = tokenRepository.findByCodigo(codigo);
        return token == null;
    }
}
