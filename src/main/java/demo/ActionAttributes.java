package demo;

//Anna

/*
	
	
{
	"streetName": "Sheshet Hayamim",
	"cityName": "Binyamina",	
	"isParkingEmpty": false,
	"nameOfParking": "parking name"
} 	
 */

public class ActionAttributes {
	private String streetName;
	private String cityName;
	private Boolean isParkingEmpty;
	private String nameOfParking;

	public ActionAttributes() {

	}

	public ActionAttributes(String streetName, String cityName, boolean isParkingEmpty, String nameOfParking) {
		super();
		this.streetName = streetName;
		this.cityName = cityName;
		this.isParkingEmpty = isParkingEmpty;
		this.nameOfParking = nameOfParking;
	}

	public String getStreetName() {
		return streetName;
	}

	public void setStreetName(String streetName) {
		this.streetName = streetName;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public boolean isParkingEmpty() {
		return isParkingEmpty;
	}

	public void setParkingEmpty(boolean isParkingEmpty) {
		this.isParkingEmpty = isParkingEmpty;
	}

	public String getNameOfParking() {
		return nameOfParking;
	}

	public void setNameOfParking(String nameOfParking) {
		this.nameOfParking = nameOfParking;
	}

}
