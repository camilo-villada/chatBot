package com.riwi.librotech;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.riwi.librotech.Repository.LibroRepository;
import com.riwi.librotech.model.Libro;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class LibroLaboratorioPerformanceTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Statistics statistics;

    @BeforeEach
    void setUp() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();
    }

    @Test
    void apiLibrosConPaginacionRetornaMetadatosYRespetaMaximo() throws Exception {
        mockMvc.perform(get("/api/libros")
                        .param("page", "0")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(50))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrevious").value(false))
                .andExpect(jsonPath("$.libros", hasSize(50)));
    }

    @Test
    void apiLibrosConSoloPageUsaTamanoPorDefecto() throws Exception {
        mockMvc.perform(get("/api/libros")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(20))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrevious").value(false))
                .andExpect(jsonPath("$.libros", hasSize(20)));
    }

    @Test
    void sliceEvitaCountYPageEjecutaConsultaAdicional() {
        Slice<?> slice = libroRepository.findResumenes(PageRequest.of(0, 10));
        slice.getContent().size();
        assertTrue(slice.hasNext());

        long sliceStatements = statistics.getPrepareStatementCount();

        statistics.clear();

        Page<?> page = libroRepository.findResumenesPage(PageRequest.of(0, 10));
        page.getContent().size();
        assertTrue(page.getTotalElements() > 0);

        long pageStatements = statistics.getPrepareStatementCount();

        assertEquals(1L, sliceStatements);
        assertEquals(2L, pageStatements);
    }

    @Test
    void entityGraphYJoinFetchInicializanRelacionesSinNMasUno() {
        List<Libro> libros = libroRepository.findAllWithRelaciones();
        libros.stream().limit(5).forEach(libro -> {
            libro.getEditorial().getNombre();
            libro.getGeneros().size();
        });

        long entityGraphStatements = statistics.getPrepareStatementCount();
        assertEquals(1L, entityGraphStatements);

        statistics.clear();

        Libro libro = libroRepository.findById(1L).orElseThrow();
        libro.getEditorial().getNombre();
        libro.getGeneros().size();

        long findByIdStatements = statistics.getPrepareStatementCount();
        assertEquals(1L, findByIdStatements);

        statistics.clear();

        Libro detalle = libroRepository.findDetalleByIdJoinFetch(1L).orElseThrow();
        detalle.getEditorial().getNombre();
        detalle.getGeneros().size();

        long joinFetchStatements = statistics.getPrepareStatementCount();
        assertEquals(1L, joinFetchStatements);
    }
}
