package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MeberApiController {

    private final MemberService memberService;

    @GetMapping("/")
    public Result<MemberDto> members() {
        List<Member> members = memberService.findMembers();

        List<MemberDto> memberDtos = members.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(memberDtos);
    }

    @PostMapping("/")
    public CreateMemberResponse saveMember(@RequestBody CreateMemberRequest request) {
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);

        return new CreateMemberResponse(id);
    }

    @PutMapping("/{id}")
    public UpdateMemberResponse saveMember(Long id, @RequestBody UpdateMemberRequest request) {
        memberService.update(id, request.getName());

        Member member = memberService.findOne(id);
        return new UpdateMemberResponse(member.getId(), member.getName());
    }

    /*
    * API에 맞는 별도 DTO를 사용
    *   - 엔티티는 공통으로 사용하거나 비즈니스적인 요소만 가지고 있어야 하기 때문에 유효성에 대한 내용을 넣기엔 적합하지 않다. DTO를 사용
    *   - 엔티티 변경이 있어도 API 스펙은 변경하지 않을 수 있다.
    * */
    @Data
    static class CreateMemberRequest {
        private String name;
    }

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

    @Data
    static class UpdateMemberRequest {
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse {
        private Long id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }


}
