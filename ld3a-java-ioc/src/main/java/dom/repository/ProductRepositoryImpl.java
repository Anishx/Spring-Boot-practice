package dom.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import dom.model.Product;

//ProductRepository productRepository = new ProductRepositoryImpl();
@Component("productRepository")
public class ProductRepositoryImpl implements ProductRepository {

	@Override
	public List<Product> findAll() {
//		A STUB FOR PRODUCTS
		List<Product> products = new ArrayList<>();
		products.add(new Product("P01", "Man's Search For Meaning", 300));
		products.add(new Product("P02", "Awaken the Giant Within", 500));
		return products;
	}
}
