package nl.knaw.huygens.alexandria.model;

import java.util.ArrayList;
import java.util.List;

public class BaseLayer {

	List<BaseElement> baseElements = new ArrayList<>();

	public void setBaseElements(List<BaseElement> baseElements) {
    this.baseElements = baseElements;
  }

  public List<BaseElement> getBaseElements() {
		return baseElements;
	}

	public void addBaseElement(BaseElement baseElement) {
		baseElements.add(baseElement);
	}

	public static class BaseElement {
		String name = "";
		List<String> baseAttributes = new ArrayList<>();

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<String> getBaseAttributes() {
			return baseAttributes;
		}

		public void setBaseAttributes(List<String> baseAttributes) {
			this.baseAttributes = baseAttributes;
		}
	}

}
