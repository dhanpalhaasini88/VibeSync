package com.vibesync;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BackendApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void songBrowsingIsPublicButCatalogWritesRequireAdmin() throws Exception {
		mockMvc.perform(get("/api/songs"))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/songs")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{"))
				.andExpect(status().isForbidden());
	}

	@Test
	void regularUsersCannotModifySongCatalog() throws Exception {
		mockMvc.perform(post("/api/songs")
					.with(user("listener").roles("USER"))
					.contentType(MediaType.APPLICATION_JSON)
					.content("{"))
				.andExpect(status().isForbidden());
	}

	@Test
	void adminsCanReachSongCatalogWriteRoute() throws Exception {
		mockMvc.perform(post("/api/songs")
					.with(user("catalog-admin").roles("ADMIN"))
					.contentType(MediaType.APPLICATION_JSON)
					.content("{"))
				.andExpect(status().isBadRequest());
	}

}
