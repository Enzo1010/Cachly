package br.com.cachly.backend.usuario.aluno;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping
    public ResponseEntity<Page<RankingResponse>> listarRanking(
            @PageableDefault(size = 20, sort = {"xpTotal", "nivel"}, direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<RankingResponse> ranking = rankingService.listarRanking(pageable);
        return ResponseEntity.ok(ranking);
    }
}
