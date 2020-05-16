package acs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import acs.data.TypeEnum;
import acs.data.UserRole;
import acs.rest.boundaries.element.ElementBoundary;
import acs.rest.boundaries.element.ElementIdBoundary;
import acs.rest.boundaries.element.Location;
import acs.rest.boundaries.user.NewUserDetailsBoundary;
import acs.rest.boundaries.user.UserBoundary;
import acs.rest.boundaries.user.UserIdBoundary;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ElementTests {
	private RestTemplate restTemplate;
	private String url;
	private int port;

	@LocalServerPort
	public void setPort(int port) {
		this.port = port;
	}

	@PostConstruct
	public void init() {
		this.restTemplate = new RestTemplate();
		this.url = "http://localhost:" + this.port + "/acs";
	}

	@AfterEach
	@BeforeEach
	public void teardown() {
		this.restTemplate.delete(this.url + "/admin/elements/{adminDomain}/{adminEmail}", "???", "??");
	}

	@Test
	public void testContext() {

	}

	@Test
	public void test_Create_New_Element_And_Check_If_DB_Contatins_Same_ElementID() throws Exception {
		ElementBoundary eb = new ElementBoundary(new ElementIdBoundary(), TypeEnum.actionType.name(), "moshe", true,
				new Date(), new Location(), null, null);
		NewUserDetailsBoundary ub = new NewUserDetailsBoundary("demo@us.er", UserRole.MANAGER, "demo1", ":(");
		UserBoundary postedUB = this.restTemplate.postForObject(this.url + "/users", ub, UserBoundary.class);

		ElementIdBoundary postedElementId = this.restTemplate.postForObject(
				this.url + "/elements/" + postedUB.getUserId().getDomain() + "/" + postedUB.getUserId().getEmail(), eb,
				ElementBoundary.class).getElementId();

		ElementBoundary[] allElements = this.restTemplate.getForObject(
				this.url + "/elements/" + postedUB.getUserId().getDomain() + "/" + postedUB.getUserId().getEmail(),
				ElementBoundary[].class);
		boolean exist = false;
		for (ElementBoundary element : allElements)
			if (postedElementId.getId().equals(element.getElementId().getId()))
				exist = true;
		if (!exist)
			throw new Exception("not found");

	}

	@Test
	public void test_Create_New_Element_And_Check_If_DB_Contatins_Exactly_One_Element() throws Exception {
		ElementBoundary eb = new ElementBoundary(new ElementIdBoundary(), TypeEnum.actionType.name(), "moshe", true,
				new Date(), new Location(), null, null);
		NewUserDetailsBoundary ub = new NewUserDetailsBoundary("demo@us.er", UserRole.MANAGER, "demo1", ":(");
		UserBoundary postedUB = this.restTemplate.postForObject(this.url + "/users", ub, UserBoundary.class);

		this.restTemplate.postForObject(
				this.url + "/elements/" + postedUB.getUserId().getDomain() + "/" + postedUB.getUserId().getEmail(), eb,
				ElementBoundary.class).getElementId();

		ElementBoundary[] allElements = this.restTemplate.getForObject(
				this.url + "/elements/" + postedUB.getUserId().getDomain() + "/" + postedUB.getUserId().getEmail(),
				ElementBoundary[].class);
		if (allElements.length != 1)
			throw new Exception("error");
	}

	@Test
	public void test_Create_New_Element_And_User_Is_Not_Manager() throws Exception {
		// GIVEN the server is up
		// do nothing

		// WHEN I POST new element and new user
		ElementBoundary eb = new ElementBoundary(new ElementIdBoundary(), TypeEnum.actionType.name(), "moshe", true,
				new Date(), new Location(), null, null);

		NewUserDetailsBoundary ub = new NewUserDetailsBoundary("demo@us.er", UserRole.PLAYER, "demo1", ":("); // create
																												// PLAYER
																												// user
		UserBoundary postedUB = this.restTemplate.postForObject(this.url + "/users", ub, UserBoundary.class);

		// THEN the server responds with status <> 2xx
		// exception because the user is PLAYER (and not MANAGER)
		assertThrows(Exception.class, () -> this.restTemplate.postForObject(
				this.url + "/elements/" + postedUB.getUserId().getDomain() + "/" + postedUB.getUserId().getEmail(), eb,
				ElementBoundary.class));
	}

	@Test
	public void test_Create_Two_Elements_Get_Specific_One_And_See_If_ID_Matches() throws Exception {
		ElementBoundary eb1 = new ElementBoundary(new ElementIdBoundary(), TypeEnum.actionType.name(), "moshe", true,
				new Date(), new Location(), null, null);
		ElementBoundary eb2 = new ElementBoundary(new ElementIdBoundary(), TypeEnum.actionType.name(), "david", true,
				new Date(), new Location(), null, null);

		NewUserDetailsBoundary ub = new NewUserDetailsBoundary("demo@us.er", UserRole.MANAGER, "demo1", ":(");
		UserBoundary postedUB = this.restTemplate.postForObject(this.url + "/users", ub, UserBoundary.class);

		ElementBoundary neweb1 = this.restTemplate.postForObject(
				this.url + "/elements/" + postedUB.getUserId().getDomain() + "/" + postedUB.getUserId().getEmail(), eb1,
				ElementBoundary.class);
		this.restTemplate.postForObject(
				this.url + "/elements/" + postedUB.getUserId().getDomain() + "/" + postedUB.getUserId().getEmail(), eb2,
				ElementBoundary.class);

		ElementBoundary ebCheck = this.restTemplate.getForObject(
				this.url + "/elements/" + postedUB.getUserId().getDomain() + "/" + postedUB.getUserId().getEmail() + "/"
						+ neweb1.getElementId().getDomain() + "/" + neweb1.getElementId().getId(),
				ElementBoundary.class);

		if (!ebCheck.getElementId().getId().equals(neweb1.getElementId().getId()))
			throw new Exception("error");
	}

	@Test
	public void test_Create_Two_Elements_Delete_All_Elements_And_Check_If_Delete_Succeeded() throws Exception {
		ElementBoundary eb1 = new ElementBoundary(new ElementIdBoundary(), TypeEnum.actionType.name(), "moshe", true,
				new Date(), new Location(), null, null);
		ElementBoundary eb2 = new ElementBoundary(new ElementIdBoundary(), TypeEnum.actionType.name(), "david", true,
				new Date(), new Location(), null, null);
		NewUserDetailsBoundary ub = new NewUserDetailsBoundary("demo@us.er", UserRole.MANAGER, "demo1", ":(");
		UserBoundary postedUB = this.restTemplate.postForObject(this.url + "/users", ub, UserBoundary.class);
		this.restTemplate.postForObject(
				this.url + "/elements/" + postedUB.getUserId().getDomain() + "/" + postedUB.getUserId().getEmail(), eb1,
				ElementBoundary.class);
		this.restTemplate.postForObject(
				this.url + "/elements/" + postedUB.getUserId().getDomain() + "/" + postedUB.getUserId().getEmail(), eb2,
				ElementBoundary.class);

		this.restTemplate.delete(this.url + "/admin/elements/{adminDomain}/{adminEmail}",
				postedUB.getUserId().getDomain(), postedUB.getUserId().getEmail());

		ElementBoundary[] allElements = this.restTemplate.getForObject(
				this.url + "/elements/" + postedUB.getUserId().getDomain() + "/" + postedUB.getUserId().getEmail(),
				ElementBoundary[].class);
		if (allElements.length != 0)
			throw new Exception("error, delete failed");
	}

	@Test
	public void test_Update_Element_And_Check_If_Update_Succeeded() throws Exception {

		NewUserDetailsBoundary ub = new NewUserDetailsBoundary("demo@us.er", UserRole.MANAGER, "demo1", ":(");
		UserBoundary postedUB = this.restTemplate.postForObject(this.url + "/users", ub, UserBoundary.class);

		ElementBoundary eb = new ElementBoundary(new ElementIdBoundary(), TypeEnum.actionType.name(), "moshe", true,
				new Date(), new Location(), null, null);
		ElementIdBoundary postedElementId = this.restTemplate.postForObject(
				this.url + "/elements/" + postedUB.getUserId().getDomain() + "/" + postedUB.getUserId().getEmail(), eb,
				ElementBoundary.class).getElementId();
		eb.setName("new_name");

		this.restTemplate.put(this.url + "/elements/2020b.tamir.reznik/demo@us.er/{elementDomain}/{elementId}", eb,
				postedElementId.getDomain(), postedElementId.getId());

		ElementBoundary[] allElements = this.restTemplate
				.getForObject(this.url + "/elements/2020b.tamir.reznik/demo@us.er", ElementBoundary[].class);
		if (!allElements[0].getName().equals("new_name"))
			throw new Exception("error");
	}

//	//NOT WORK
//	@Test
//	public void test_Update_Element_And_User_Is_Not_Manager() throws Exception {
//		
//		NewUserDetailsBoundary ub = new NewUserDetailsBoundary("demo@us.er", UserRole.MANAGER, "demo1", ":(");
//		UserBoundary postedUB = this.restTemplate.postForObject(this.url +"/users", ub, UserBoundary.class);
//		
//		ElementBoundary eb = new ElementBoundary(new ElementIdBoundary(), TypeEnum.actionType.name(), "moshe", true,
//				new Date(), new Location(), null, null);
//		
////		UserIdBoundary manager = this.restTemplate.postForObject(this.url + "/users",
////				new NewUserDetailsBoundary("Sapir@gmail.com", UserRole.MANAGER, "sapir", ":-)"),UserBoundary.class).getUserId();	
////			this.restTemplate.postForObject(this.url + "/elements/"+manager.getDomain()+"/"+manager.getEmail(), eb, ElementBoundary.class);
//		
//		//domain and id of element	
//		String domain = eb.getElementId().getDomain();
//		String id = eb.getElementId().getId();
//	
//		//ElementIdBoundary postedElementId = this.restTemplate
//		//		.postForObject(this.url + "/elements/"+postedUB.getUserId().getDomain()+"/"+postedUB.getUserId().getEmail(), eb, ElementBoundary.class).getElementId();
//		eb.setName("new_name");
//
//		//The server responds with status <> 2xx
//		//exception because the user is PLAYER (and not MANAGER)
//		
//	assertThrows(Exception.class, ()->	
//			this.restTemplate.put(this.url + "/elements/" + postedUB.getUserId().getDomain()+ "/"
//					+postedUB.getUserId().getEmail() + "/{elementDomain}/{elementId}",
//				 eb,domain, id));
//	}

	@Test
	public void test_Create_Three_Elements_Bind_Them_And_Validate_Relation() throws Exception {
		// GIVEN the server is up
		// WHEN we create 3 elements
		ElementBoundary parent = new ElementBoundary(new ElementIdBoundary(), TypeEnum.actionType.name(), "Parent",
				true, new Date(), new Location(0.5, 0.5), null, null);
		ElementBoundary child1 = new ElementBoundary(new ElementIdBoundary(), TypeEnum.actionType.name(), "child1",
				true, new Date(), new Location(0.5, 0.5), null, null);
		ElementBoundary child2 = new ElementBoundary(new ElementIdBoundary(), TypeEnum.actionType.name(), "child2",
				true, new Date(), new Location(0.5, 0.5), null, null);

		NewUserDetailsBoundary ub = new NewUserDetailsBoundary("demo@us.er", UserRole.MANAGER, "demo1", ":(");
		UserBoundary postedUB = this.restTemplate.postForObject(this.url + "/users", ub, UserBoundary.class);
		// post them
		ElementBoundary postedChild1Element = this.restTemplate.postForObject(
				this.url + "/elements/" + postedUB.getUserId().getDomain() + "/" + postedUB.getUserId().getEmail(),
				child1, ElementBoundary.class);
		ElementBoundary postedChild2Element = this.restTemplate.postForObject(
				this.url + "/elements/" + postedUB.getUserId().getDomain() + "/" + postedUB.getUserId().getEmail(),
				child2, ElementBoundary.class);
		ElementBoundary postedParentElement = this.restTemplate.postForObject(
				this.url + "/elements/" + postedUB.getUserId().getDomain() + "/" + postedUB.getUserId().getEmail(),
				parent, ElementBoundary.class);

		List<ElementBoundary> allChildBeforeBind = new ArrayList<>();
		allChildBeforeBind.add(postedChild1Element);
		allChildBeforeBind.add(postedChild2Element);

		List<ElementBoundary> allParentsBeforeBind = new ArrayList<>();
		allParentsBeforeBind.add(postedParentElement);

		// bind children to the parent
		this.restTemplate.put(
				this.url + "/elements/{managerDomain}/{managerEmail}/{elementDomain}/{elementId}/children",
				postedChild1Element.getElementId(), "???", "???", postedParentElement.getElementId().getDomain(),
				postedParentElement.getElementId().getId());
		this.restTemplate.put(
				this.url + "/elements/{managerDomain}/{managerEmail}/{elementDomain}/{elementId}/children",
				postedChild2Element.getElementId(), "???", "???", postedParentElement.getElementId().getDomain(),
				postedParentElement.getElementId().getId());

		// AND get all children
		ElementBoundary[] allChilds = this.restTemplate.getForObject(
				this.url + "/elements/{managerDomain}/{managerEmail}/{elementDomain}/{elementId}/children",
				ElementBoundary[].class, "???", "???", postedParentElement.getElementId().getDomain(),
				postedParentElement.getElementId().getId());

		// AND get all parents
		ElementBoundary[] allParents = this.restTemplate.getForObject(
				this.url + "/elements/{managerDomain}/{managerEmail}/{elementDomain}/{elementId}/parents",
				ElementBoundary[].class, "???", "???", postedChild1Element.getElementId().getDomain(),
				postedChild1Element.getElementId().getId());

		// THEN we get the same array with the childrens
		assertThat(allChilds).hasSize(allChildBeforeBind.size()).usingRecursiveFieldByFieldElementComparator()
				.containsExactlyInAnyOrderElementsOf(allChildBeforeBind);

		// THEN we get the same array with the Parents
		assertThat(allParents).usingRecursiveFieldByFieldElementComparator()
				.containsExactlyInAnyOrderElementsOf(allParentsBeforeBind);

	}

	@Test
	public void test_Create_One_Element_Check_For_Empty_Array_In_Childrens_Of_Element() throws Exception {
		// GIVEN the server is up
		ElementBoundary element = new ElementBoundary(new ElementIdBoundary(), TypeEnum.actionType.name(), "Parent",
				true, new Date(), new Location(0.5, 0.5), null, null);

		NewUserDetailsBoundary ub = new NewUserDetailsBoundary("demo@us.er", UserRole.MANAGER, "demo1", ":(");
		UserBoundary postedUB = this.restTemplate.postForObject(this.url + "/users", ub, UserBoundary.class);

		// WHEN we post the element
		ElementBoundary postedElement = this.restTemplate.postForObject(
				this.url + "/elements/" + postedUB.getUserId().getDomain() + "/" + postedUB.getUserId().getEmail(),
				element, ElementBoundary.class);

		// get all children of the element
		ElementBoundary[] allChilds = this.restTemplate.getForObject(
				this.url + "/elements/{managerDomain}/{managerEmail}/{elementDomain}/{elementId}/children",
				ElementBoundary[].class, "???", "???", postedElement.getElementId().getDomain(),
				postedElement.getElementId().getId());

		// THEN we get an empty array
		assertThat(allChilds).isEmpty();

	}

	@Test
	public void test_Create_One_Element_Check_For_Empty_Array_In_Parents_Of_Element() throws Exception {
		// GIVEN the server is up
		ElementBoundary element = new ElementBoundary(new ElementIdBoundary(), TypeEnum.actionType.name(), "Parent",
				true, new Date(), new Location(0.5, 0.5), null, null);
		NewUserDetailsBoundary ub = new NewUserDetailsBoundary("demo@us.er", UserRole.MANAGER, "demo1", ":(");
		UserBoundary postedUB = this.restTemplate.postForObject(this.url + "/users", ub, UserBoundary.class);

		// WHEN we post the element
		ElementBoundary postedElement = this.restTemplate.postForObject(
				this.url + "/elements/" + postedUB.getUserId().getDomain() + "/" + postedUB.getUserId().getEmail(),
				element, ElementBoundary.class);

		// get all parents of the element
		ElementBoundary[] allChilds = this.restTemplate.getForObject(
				this.url + "/elements/{userDomain}/{userEmail}/{elementDomain}/{elementId}/parents",
				ElementBoundary[].class, "???", "???", postedElement.getElementId().getDomain(),
				postedElement.getElementId().getId());

		// THEN we get an empty array
		assertThat(allChilds).isEmpty();

	}

	@Test
	public void testGetAllElementsWithBadSize() throws Exception {

		// GIVEN the database contains 3 Element
		ElementBoundary eb1 = new ElementBoundary(new ElementIdBoundary(), TypeEnum.actionType.name(), "element1", true,
				new Date(), new Location(), null, null);
		ElementBoundary eb2 = new ElementBoundary(new ElementIdBoundary(), TypeEnum.actionType.name(), "element2", true,
				new Date(), new Location(), null, null);
		ElementBoundary eb3 = new ElementBoundary(new ElementIdBoundary(), TypeEnum.actionType.name(), "element3", true,
				new Date(), new Location(), null, null);

		// create MANAGER user
		UserIdBoundary manager = this.restTemplate.postForObject(this.url + "/users",
				new NewUserDetailsBoundary("Sapir@gmail.com", UserRole.MANAGER, "sapir", ":-)"), UserBoundary.class)
				.getUserId();
		this.restTemplate.postForObject(this.url + "/elements/" + manager.getDomain() + "/" + manager.getEmail(), eb1,
				ElementBoundary.class);
		this.restTemplate.postForObject(this.url + "/elements/" + manager.getDomain() + "/" + manager.getEmail(), eb2,
				ElementBoundary.class);
		this.restTemplate.postForObject(this.url + "/elements/" + manager.getDomain() + "/" + manager.getEmail(), eb3,
				ElementBoundary.class);

		// WHEN I invoke GET /acs/elements/{userDomain}/{userEmail}?page=5&size=-1
		// THEN the server responds with status <> 2xx
		assertThrows(Exception.class,
				() -> this.restTemplate.getForObject(
						this.url + "/elements/{userDomain}/{userEmail}?page={page}&size={size}",
						ElementBoundary[].class, manager.getDomain(), manager.getEmail(), 5, // page
						-1 // size
				));
	}

	@Test
	public void testSearchByLoactionViaAdminUserAndGetUnauthorizedException() throws Exception {

// 		GIVEN the database contains 2 users - admin and manger
//		manager create element
		NewUserDetailsBoundary User = new NewUserDetailsBoundary("managerTamir@afeka.ac.il", UserRole.MANAGER,
				"managerTamir", ":>");

		UserBoundary managerUser = this.restTemplate.postForObject(this.url + "/users", User, UserBoundary.class);

		User = new NewUserDetailsBoundary("adminTamir@afeka.ac.il", UserRole.ADMIN, "adminTamir", ":<");

		UserBoundary adminUser = this.restTemplate.postForObject(this.url + "/users", User, UserBoundary.class);

		ElementBoundary element = new ElementBoundary(new ElementIdBoundary(), TypeEnum.actionType.name(), "Parent",
				true, new Date(), new Location(0.5, 0.5), null, null);

//		WHEN Manager post the element
		ElementBoundary postElements = this.restTemplate.postForObject(this.url + "/elements/"
				+ managerUser.getUserId().getDomain() + "/" + managerUser.getUserId().getEmail(), element,
				ElementBoundary.class);

//		Admin Cannot search elements by location...
		assertThrows(HttpClientErrorException.Unauthorized.class,
				() -> this.restTemplate.getForObject(
						this.url + "/elements/" + adminUser.getUserId().getDomain() + "/"
								+ adminUser.getUserId().getEmail() + "/search/near/1/1/10",
						ElementBoundary[].class, adminUser.getUserId().getDomain(), adminUser.getUserId().getEmail()));

		ElementBoundary[] getElements = this.restTemplate.getForObject(
				this.url + "/elements/" + managerUser.getUserId().getDomain() + "/" + managerUser.getUserId().getEmail()
						+ "/search/near/1/1/10",
				ElementBoundary[].class, managerUser.getUserId().getDomain(), managerUser.getUserId().getEmail());

//		Manager Can search elements by location...
		assertThat(getElements).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(postElements);
	}

	@Test
	public void testGetSpecificInactiveElementViaPlayerUserAndGetNotFoundException() throws Exception {

// 		GIVEN the database contains 2 users - player and manger
//		manager create element
		NewUserDetailsBoundary User = new NewUserDetailsBoundary("managerTamir@afeka.ac.il", UserRole.MANAGER,
				"managerTamir", ":>");

		UserBoundary managerUser = this.restTemplate.postForObject(this.url + "/users", User, UserBoundary.class);

		User = new NewUserDetailsBoundary("playerTamir@afeka.ac.il", UserRole.PLAYER, "playerTamir", ":<");

		UserBoundary playerUser = this.restTemplate.postForObject(this.url + "/users", User, UserBoundary.class);

		ElementBoundary element = new ElementBoundary(new ElementIdBoundary(), TypeEnum.actionType.name(), "Parent",
				false, new Date(), new Location(0.5, 0.5), null, null);

//		WHEN Manager post the element
		ElementBoundary postElement = this.restTemplate.postForObject(this.url + "/elements/"
				+ managerUser.getUserId().getDomain() + "/" + managerUser.getUserId().getEmail(), element,
				ElementBoundary.class);

//		Player Cannot get specific inActive element
		assertThrows(HttpClientErrorException.NotFound.class, () -> this.restTemplate.getForObject(
				this.url + "/elements/" + playerUser.getUserId().getDomain() + "/" + playerUser.getUserId().getEmail()
						+ "/" + postElement.getElementId().getDomain() + "/" + postElement.getElementId().getId(),
				ElementBoundary.class));

		ElementBoundary[] getElements = this.restTemplate.getForObject(
				this.url + "/elements/" + managerUser.getUserId().getDomain() + "/"
						+ managerUser.getUserId().getEmail(),
				ElementBoundary[].class, managerUser.getUserId().getDomain(), managerUser.getUserId().getEmail());

//		Manager Can get specific element
		assertThat(getElements).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(postElement);
	}

	@Test
	public void testGetSpecificActiveElementViaAdminUserAndGetUnauthorizedException() throws Exception {

// 		GIVEN the database contains 2 users - player and manger
//		manager create element
		NewUserDetailsBoundary User = new NewUserDetailsBoundary("managerTamir@afeka.ac.il", UserRole.MANAGER,
				"managerTamir", ":>");

		UserBoundary managerUser = this.restTemplate.postForObject(this.url + "/users", User, UserBoundary.class);

		User = new NewUserDetailsBoundary("adminTamir@afeka.ac.il", UserRole.ADMIN, "adminTamir", ":<");

		UserBoundary adminUser = this.restTemplate.postForObject(this.url + "/users", User, UserBoundary.class);

		ElementBoundary element = new ElementBoundary(new ElementIdBoundary(), TypeEnum.actionType.name(), "Parent",
				true, new Date(), new Location(0.5, 0.5), null, null);

//		WHEN Manager post the element
		ElementBoundary postElement = this.restTemplate.postForObject(this.url + "/elements/"
				+ managerUser.getUserId().getDomain() + "/" + managerUser.getUserId().getEmail(), element,
				ElementBoundary.class);

//		Admin Cannot get specific element
		assertThrows(HttpClientErrorException.Unauthorized.class, () -> this.restTemplate.getForObject(
				this.url + "/elements/" + adminUser.getUserId().getDomain() + "/" + adminUser.getUserId().getEmail()
						+ "/" + postElement.getElementId().getDomain() + "/" + postElement.getElementId().getId(),
				ElementBoundary.class));

		ElementBoundary[] getElements = this.restTemplate.getForObject(
				this.url + "/elements/" + managerUser.getUserId().getDomain() + "/"
						+ managerUser.getUserId().getEmail(),
				ElementBoundary[].class, managerUser.getUserId().getDomain(), managerUser.getUserId().getEmail());

//		Manager Can get specific element
		assertThat(getElements).usingRecursiveFieldByFieldElementComparator().containsExactlyInAnyOrder(postElement);
	}

	@Test
	public void testGetElementArrayViaPlayerUserAndGetOnlyActiveElements() {

//	note - total_Elements_To_Post need to be even
		int total_Elements_To_Post = 16;
		int total_Active_Elements = total_Elements_To_Post / 2;

// 		GIVEN the database contains 2 users - player and manger
		NewUserDetailsBoundary User = new NewUserDetailsBoundary("managerTamir@afeka.ac.il", UserRole.MANAGER,
				"managerTamir", ":>");

		UserBoundary managerUser = this.restTemplate.postForObject(this.url + "/users", User, UserBoundary.class);

		User = new NewUserDetailsBoundary("playerTamir@afeka.ac.il", UserRole.PLAYER, "playerTamir", ":<");

		UserBoundary playerUser = this.restTemplate.postForObject(this.url + "/users", User, UserBoundary.class);

		// WHEN post 8 active elements and 8 inactive elements and save them in a list
		List<ElementBoundary> postedElements = IntStream
				.range(0, total_Elements_To_Post).mapToObj(i -> new ElementBoundary(new ElementIdBoundary(),
						"parking lot", Integer.toString(i), true, new Date(), new Location(0.5, 0.5), null, null))
				.filter(Boundary -> {
					if (Integer.valueOf(Boundary.getName()) % 2 == 0)
						Boundary.setActive(false);
					return this.restTemplate.postForObject(this.url + "/elements/" + managerUser.getUserId().getDomain()
							+ "/" + managerUser.getUserId().getEmail(), Boundary, ElementBoundary.class) != null;
				}).collect(Collectors.toList());
//		Player get all elements and receive only active elements from db 
		ElementBoundary[] playerGetElements = this.restTemplate.getForObject(
				this.url + "/elements/" + playerUser.getUserId().getDomain() + "/" + playerUser.getUserId().getEmail()
						+ "?page=0&size=16",
				ElementBoundary[].class, playerUser.getUserId().getDomain(), playerUser.getUserId().getEmail());

		ElementBoundary[] managerGetElements = this.restTemplate.getForObject(
				this.url + "/elements/" + managerUser.getUserId().getDomain() + "/" + managerUser.getUserId().getEmail()
						+ "?page=0&size=16",
				ElementBoundary[].class, managerUser.getUserId().getDomain(), managerUser.getUserId().getEmail());

		assertThat(playerGetElements).hasSize(total_Active_Elements);

		for (ElementBoundary elementBoundary : playerGetElements)
			assertThat(elementBoundary.getActive()).isTrue();

//		assertThat(managerGetElements).usingRecursiveFieldByFieldElementComparator()
//				.containsExactlyInAnyOrderElementsOf(postedElements);
	}
}
