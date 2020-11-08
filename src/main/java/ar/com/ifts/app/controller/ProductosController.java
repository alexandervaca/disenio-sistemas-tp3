package ar.com.ifts.app.controller;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.time.LocalDate;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.com.ifts.app.exception.ProductosServiceException;
import ar.com.ifts.app.model.input.RequestCrearProductoBody;
import ar.com.ifts.app.model.input.RequestModificarProductoBody;
import ar.com.ifts.app.model.output.GetProductoResponse;
import ar.com.ifts.app.model.output.GetProductosResponse;
import ar.com.ifts.app.model.output.ProductoResponse;
import ar.com.ifts.app.model.output.dto.ProductoBuilder;
import ar.com.ifts.app.services.ProductosService;

@RestController
@RequestMapping(value = "/api")
public class ProductosController {

	@Autowired
	private ProductosService productosService;

	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_PROVEEDOR') or hasRole('ROLE_CLIENTE')")
	@GetMapping(value = "/productos", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<GetProductosResponse> obtenerProductos() {
		return ResponseEntity.ok(new GetProductosResponse("Consulta de productos exitosa.",
				String.valueOf(OK.ordinal()), LocalDate.now(), productosService.getProductos().stream()
						.map(elem -> new ProductoBuilder().setProducto(elem).build()).collect(Collectors.toList())));
	}

	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_PROVEEDOR') or hasRole('ROLE_CLIENTE')")
	@GetMapping(value = "/productos/{id}", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<GetProductoResponse> obtenerProducto(@PathVariable("id") Long idProducto)
			throws ProductosServiceException {
		return ResponseEntity.ok(new GetProductoResponse("Consulta de producto exitosa.", String.valueOf(OK.ordinal()),
				LocalDate.now(), new ProductoBuilder().setProducto(productosService.obtenerProductoPorId(idProducto)).build()));
	}

	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_PROVEEDOR')")
	@PostMapping(value = "/productos", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ProductoResponse> crearProducto(
			@Valid @RequestBody RequestCrearProductoBody requestProductoBody) throws ProductosServiceException {
		productosService.create(requestProductoBody);
		return ResponseEntity.ok(
				new ProductoResponse("Creacion de producto exitosa.", String.valueOf(OK.ordinal()), LocalDate.now()));
	}

	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_PROVEEDOR')")
	@PutMapping(value = "/productos", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ProductoResponse> modificarProducto(
			@Valid @RequestBody RequestModificarProductoBody requestModificarProductoBody)
			throws ProductosServiceException {
		productosService.modificarProducto(requestModificarProductoBody);
		return ResponseEntity.ok(new ProductoResponse("Modificación de producto exitosa.", String.valueOf(OK.ordinal()),
				LocalDate.now()));
	}

	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_PROVEEDOR')")
	@DeleteMapping(value = "/productos/{idProducto}", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<ProductoResponse> eliminarProducto(@PathVariable Long idProducto)
			throws ProductosServiceException {
		productosService.deleteProducto(idProducto);
		return ResponseEntity
				.ok(new ProductoResponse("Baja de producto exitosa.", String.valueOf(OK.ordinal()), LocalDate.now()));
	}
}
