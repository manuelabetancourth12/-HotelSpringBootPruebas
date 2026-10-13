package com.hotel.Hotel.service;

import com.hotel.Hotel.domain.*;
import com.hotel.Hotel.dto.response.ReservaResponse;
import com.hotel.Hotel.exception.RecursoNoEncontradoException;
import com.hotel.Hotel.mapper.ReservaMapper;
import com.hotel.Hotel.repository.ReservaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ReservaMapper reservaMapper;

    @InjectMocks
    private ReservaService reservaService;

    @Nested
    @DisplayName("Operaciones de Confirmación")
    class ConfirmacionTests {

        // PRUEBA 1 
        @Test
        @DisplayName("Debe confirmar exitosamente cuando la reserva existe y está PENDIENTE")
        void debeConfirmarReservaExitosamente() {
            // Arrange
            UUID reservaId = UUID.randomUUID();
            Cliente cliente = new Cliente("Beatriz Morales", "beatriz@empresa.com");
            Habitacion habitacion = new HabitacionEstandar("201", 2, 200.0, 1);

            RangoFechas fechas = new RangoFechas(
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(3));
            Reserva reservaExistente = new Reserva(cliente, habitacion, fechas);

            ReservaResponse responseEsperado = new ReservaResponse(
                    reservaId,
                    "Beatriz Morales",
                    "201",
                    fechas.fechaInicio(),
                    fechas.fechaFin(),
                    400.0,
                    "CONFIRMADA");

            // Stubs
            when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reservaExistente));
            when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseEsperado);

            // Act
            ReservaResponse resultado = reservaService.confirmar(reservaId);

            // Assert
            assertNotNull(resultado);
            assertEquals("CONFIRMADA", resultado.estado());
            assertEquals(EstadoReserva.CONFIRMADA, reservaExistente.getEstado());

            // Verificación
            verify(reservaRepository, times(1)).findById(reservaId);
            verify(reservaRepository, times(1)).save(reservaExistente);
            verify(reservaMapper, times(1)).toResponse(reservaExistente);
        }

        // PRUEBA 2
        @Test
        @DisplayName("Debe lanzar excepción al intentar confirmar una reserva inexistente")
        void lanzaExcepcionAlConfirmarReservaInexistente() {
            // Arrange
            UUID reservaId = UUID.randomUUID();
            when(reservaRepository.findById(reservaId)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, () -> {
                reservaService.confirmar(reservaId);
            });

            assertEquals("Reserva no encontrada", excepcion.getMessage());
            verify(reservaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Operaciones de Cancelación")
    class CancelacionTests {

        // PRUEBA 3
        @Test
        @DisplayName("Debe cancelar exitosamente una reserva existente")
        void debeCancelarReservaExitosamente() {
            // Arrange
            UUID reservaId = UUID.randomUUID();
            Cliente cliente = new Cliente("Carlos Ruiz", "carlos@empresa.com");
            Habitacion habitacion = new HabitacionEstandar("301", 2, 150.0, 1);
            RangoFechas fechas = new RangoFechas(LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(5));
            Reserva reservaExistente = new Reserva(cliente, habitacion, fechas);

            ReservaResponse responseEsperado = new ReservaResponse(
                    reservaId, "Carlos Ruiz", "301", fechas.fechaInicio(), fechas.fechaFin(), 450.0, "CANCELADA");

            when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reservaExistente));
            when(reservaRepository.save(any(Reserva.class))).thenAnswer(i -> i.getArgument(0));
            when(reservaMapper.toResponse(any(Reserva.class))).thenReturn(responseEsperado);

            // Act
            ReservaResponse resultado = reservaService.cancelar(reservaId);

            // Assert
            assertNotNull(resultado);
            assertEquals("CANCELADA", resultado.estado());
            assertEquals(EstadoReserva.CANCELADA, reservaExistente.getEstado());
            
            verify(reservaRepository, times(1)).findById(reservaId);
            verify(reservaRepository, times(1)).save(reservaExistente);
        }
    }

    @Nested
    @DisplayName("Operaciones de Consulta")
    class ConsultaTests {

        // PRUEBA 4
        @Test
        @DisplayName("Debe retornar la reserva al buscar por un ID válido")
        void debeRetornarReservaPorId() {
            // Arrange
            UUID reservaId = UUID.randomUUID();
            Reserva reservaMock = mock(Reserva.class);
            ReservaResponse responseMock = mock(ReservaResponse.class);

            when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reservaMock));
            when(reservaMapper.toResponse(reservaMock)).thenReturn(responseMock);

            // Act
            ReservaResponse resultado = reservaService.obtenerPorId(reservaId);

            // Assert
            assertNotNull(resultado);
            verify(reservaRepository, times(1)).findById(reservaId);
            verify(reservaMapper, times(1)).toResponse(reservaMock);
        }

        // PRUEBA 5
        @Test
        @DisplayName("Debe lanzar RecursoNoEncontradoException cuando se consulta un ID que no existe")
        void lanzaExcepcionAlBuscarReservaInexistente() {
            // Arrange
            UUID reservaId = UUID.randomUUID();
            when(reservaRepository.findById(reservaId)).thenReturn(Optional.empty());

            // Act & Assert
            RecursoNoEncontradoException excepcion = assertThrows(RecursoNoEncontradoException.class, () -> {
                reservaService.obtenerPorId(reservaId);
            });

            assertTrue(excepcion.getMessage().contains("Reserva"));
            verify(reservaMapper, never()).toResponse(any(Reserva.class));
        }
    }
}