package vision.cotegory.util.baekjoon;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import vision.cotegory.exception.exception.SolvedAPiException;
import vision.cotegory.util.baekjoon.dto.SolvedAcProblemDto;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SolvedAcWebClient {

    private final WebClient webClient;

    public SolvedAcWebClient(WebClient webClient) {
        this.webClient = webClient
                .mutate()
                .baseUrl("https://solved.ac/api/v3")
                .build();
    }

    public List<SolvedAcProblemDto> getSolvedAcProblemDtosByProblemNumbers(List<Integer> problemNumbers) {
        String problemIds = problemNumbers.stream().map(String::valueOf).collect(Collectors.joining(","));

        return webClient.get()
                .uri(uri -> uri.path("/problem/lookup")
                        .queryParam("problemIds", problemIds)
                        .build())
                .retrieve()
                .onStatus(HttpStatus::isError, response -> {
                    if (response.statusCode().equals(HttpStatus.TOO_MANY_REQUESTS))
                        return Mono.error(new SolvedAPiException("SolvedAc Api 서버에 너무 많은 요청을 보냈습니다. 잠시후에 시도하세요"));
                    return Mono.error(new SolvedAPiException());
                })
                .bodyToFlux(SolvedAcProblemDto.class)
                .collectList()
                .block();
    }
}
