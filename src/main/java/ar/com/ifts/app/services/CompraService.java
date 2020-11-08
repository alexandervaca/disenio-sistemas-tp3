package ar.com.ifts.app.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.com.ifts.app.exception.CompraServiceException;
import ar.com.ifts.app.model.Compra;
import ar.com.ifts.app.model.CompraProducto;
import ar.com.ifts.app.model.Notificacion;
import ar.com.ifts.app.model.Producto;
import ar.com.ifts.app.model.Usuario;
import ar.com.ifts.app.model.input.ProductoCantidad;
import ar.com.ifts.app.model.input.RequestRealizarCompraBody;
import ar.com.ifts.app.repository.CompraProductoRepository;
import ar.com.ifts.app.repository.CompraRepository;
import ar.com.ifts.app.repository.NotificacionRepository;
import ar.com.ifts.app.repository.ProductoRepository;
import ar.com.ifts.app.repository.UsuarioRepository;

@Service
public class CompraService {

	@Autowired
	private ProductoRepository productosRepository;

	@Autowired
	private UsuarioRepository usuariosRepository;

	@Autowired
	private CompraRepository compraRepository;

	@Autowired
	private CompraProductoRepository compraProductoRepository;

	@Autowired
	private NotificacionRepository notificacionRepository;

	@Transactional(rollbackOn = { CompraServiceException.class, IllegalArgumentException.class })
	public void realizarCompra(@Valid RequestRealizarCompraBody requestRealizarCompraBody)
			throws CompraServiceException {
		Usuario cliente = usuariosRepository.findById(requestRealizarCompraBody.getClienteId())
				.orElseThrow(() -> new CompraServiceException("Cliente inexsitente."));
		Usuario proveedor = usuariosRepository.findById(requestRealizarCompraBody.getProveedorId())
				.orElseThrow(() -> new CompraServiceException("Proveedor inexistente."));
		Compra compra = compraRepository.save(new Compra(cliente, LocalDateTime.now()));
		List<CompraProducto> compraProductos = new ArrayList<>();
		for (ProductoCantidad productoCant : requestRealizarCompraBody.getProductosCantidades()) {
			Producto producto = productosRepository.findById(productoCant.getProductoId())
					.orElseThrow(() -> new CompraServiceException("Producto inexistente."));
			if (producto.getStock() < productoCant.getCantidad()) {
				throw new CompraServiceException("La cantidad ingresada es superior al existente en stock.");
			}
			compraProductos.add(compraProductoRepository
					.save(new CompraProducto(producto, compra.getIdCompra(), productoCant.getCantidad(),
							producto.getPrecio().multiply(new BigDecimal(productoCant.getCantidad())))));
		}
		BigDecimal total = compraProductos.stream().map(CompraProducto::getSubtotal).reduce(BigDecimal.ZERO,
				BigDecimal::add);
		StringBuilder sb = new StringBuilder("Se generó una compra para el usuario ");
		notificacionRepository.save(new Notificacion(compra, proveedor,
				sb.append(cliente.getNombre()).append(" por un total de ").append(total).toString()));

	}
}
