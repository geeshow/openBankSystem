package com.ken207.openbank.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ken207.openbank.common.RestDocsConfiguration;
import com.ken207.openbank.common.TestDescription;
import com.ken207.openbank.customer.Customer;
import com.ken207.openbank.domain.Branch;
import com.ken207.openbank.domain.enums.BranchType;
import com.ken207.openbank.dto.request.BranchCreateRequest;
import com.ken207.openbank.repository.BranchRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.IntStream;

import static org.junit.Assert.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
public class BranchApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BranchRepository branchRepository;

    @Test
    @TestDescription("정상적으로 지점을 생성하는 테스트")
    public void createBranch() throws Exception {
        String branchName = "테스트지점";
        String businessNumber = "123-12-12345";
        String taxOfficeCode = "112";
        String telNumber = "02-1234-1234";
        BranchType branchType = BranchType.지점;
        BranchCreateRequest branchCreateRequest = BranchCreateRequest.builder()
                .name(branchName)
                .businessNumber(businessNumber)
                .taxOfficeCode(taxOfficeCode)
                .telNumber(telNumber)
                .branchType(branchType)
                .build();

        mockMvc.perform(post("/api/branch")
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .accept(MediaTypes.HAL_JSON)
                    .content(objectMapper.writeValueAsString(branchCreateRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("regDateTime").exists())
                .andExpect(jsonPath("name").value(branchName))
                .andExpect(jsonPath("businessNumber").value(businessNumber))
                .andExpect(jsonPath("taxOfficeCode").value(taxOfficeCode))
                .andExpect(jsonPath("telNumber").value(telNumber))
                .andExpect(jsonPath("branchType").value(branchType))
                .andDo(document("create-branch",
                        links(
                                linkWithRel("self").description("link to self"),
                                linkWithRel("query-customers").description("link to query branches"),
                                linkWithRel("update-branch").description("link to update an existing branch"),
                                linkWithRel("profile").description("link to profile.")
                        ),
                        requestHeaders(
                                headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("content type header")
                        ),
                        requestFields(
                                fieldWithPath("name").description("Name of new branch"),
                                fieldWithPath("businessNumber").description("business number of new branch"),
                                fieldWithPath("taxOfficeCode").description("tax office code of new branch"),
                                fieldWithPath("telNumber").description("telephone number of new branch"),
                                fieldWithPath("branchType").description("branch type of new branch")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.LOCATION).description("Location header"),
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("HAL/JSON type content type")
                        ),
                        responseFields(
                                fieldWithPath("id").description("identifier of new branch"),
                                fieldWithPath("name").description("name of new branch"),
                                fieldWithPath("businessNumber").description("business number of new branch"),
                                fieldWithPath("taxOfficeCode").description("tax office code of new branch"),
                                fieldWithPath("telNumber").description("telephone number of new branch"),
                                fieldWithPath("regDateTime").description("registration date of new branch"),
                                fieldWithPath("branchType").description("branch type of new branch"),
                                fieldWithPath("_links.self.href").description("link to self."),
                                fieldWithPath("_links.query-branches.href").description("link to query branches."),
                                fieldWithPath("_links.update-branch.href").description("link to update existing branch."),
                                fieldWithPath("_links.profile.href").description("link to profile.")
                        )

                ))
        ;
    }

    @Test
    @TestDescription("30개의 지점을 10개씩 두번째 페이지 조회하기")
    public void queryBranches() throws Exception {
        //given
        IntStream.range(0,30).forEach(this::generateBranch);

        //when
        this.mockMvc.perform(get("/api/branch")
                .param("page", "1")
                .param("size", "10")
                .param("sort", "name,DESC")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.branchResponseList[0]._links.self").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andDo(document("query-branches",
                        links(
                                linkWithRel("first").description("link to first page"),
                                linkWithRel("prev").description("link to prev page"),
                                linkWithRel("self").description("link to self"),
                                linkWithRel("next").description("link to next page"),
                                linkWithRel("last").description("link to last page"),
                                linkWithRel("profile").description("link to profile.")
                        ),
                        requestParameters(
                                parameterWithName("page").description("현재 페이지 번호. 0페이지 부터 시작."),
                                parameterWithName("size").description("한 페이지의 사이즈"),
                                parameterWithName("sort").description("데이터 정렬. ex)name, DESC")
                        ),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("HAL/JSON type content type")
                        ),
                        responseFields(
                                fieldWithPath("_embedded.branchResponseList[0].id").description("identifier of branch"),
                                fieldWithPath("_embedded.branchResponseList[0].name").description("name of branch"),
                                fieldWithPath("_embedded.branchResponseList[0].businessNumber").description("businessNumber of branch"),
                                fieldWithPath("_embedded.branchResponseList[0].taxOfficeCode").description("taxOfficeCode of branch"),
                                fieldWithPath("_embedded.branchResponseList[0].telNumber").description("telNumber date of branch"),
                                fieldWithPath("_embedded.branchResponseList[0].regDateTime").description("regDateTime branch of branch"),
                                fieldWithPath("_embedded.branchResponseList[0].branchType").description("branchType branch of branch"),
                                fieldWithPath("_embedded.branchResponseList[0]._links.self.href").description("link to self."),
                                fieldWithPath("_links.first.href").description("link to first."),
                                fieldWithPath("_links.prev.href").description("link to prev."),
                                fieldWithPath("_links.self.href").description("link to self."),
                                fieldWithPath("_links.next.href").description("link to next."),
                                fieldWithPath("_links.last.href").description("link to last."),
                                fieldWithPath("_links.profile.href").description("link to profile."),
                                fieldWithPath("page.size").description("size of one page."),
                                fieldWithPath("page.totalElements").description("amount of datas."),
                                fieldWithPath("page.totalPages").description("amount of pages."),
                                fieldWithPath("page.number").description("current page number.")
                        )
                ));
    }

    @Test
    @TestDescription("지점 한건 조회하기")
    public void getBranch() throws Exception {
        int index = 200;
        Branch branch = generateBranch(index);

        this.mockMvc.perform(get("/api/branch/{id}", branch.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("_links.self").hasJsonPath())
                .andExpect(jsonPath("_links.update").doesNotExist())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("regDateTime").exists())
                .andExpect(jsonPath("name").value("지점이름" + index))
                .andExpect(jsonPath("businessNumber").value("bzNum" + index))
                .andExpect(jsonPath("taxOfficeCode").value("00" + index))
                .andExpect(jsonPath("telNumber").value("02-1234-1234"))
                .andExpect(jsonPath("branchType").value(BranchType.지점.toString()))
                .andDo(document("get-branch"))
        ;
    }
    private Branch generateBranch(int index) {
        Branch branch = new Branch("지점이름" + index, "bzNum" + index, "00" + index, "02-1234-1234", BranchType.지점);
        return branchRepository.save(branch);
    }

}