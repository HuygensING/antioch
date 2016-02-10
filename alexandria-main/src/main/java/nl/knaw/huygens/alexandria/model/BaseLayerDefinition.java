package nl.knaw.huygens.alexandria.model;

import java.util.ArrayList;
import java.util.List;

public class BaseLayerDefinition {

	List<BaseElementDefinition> baseElementDefinitions = new ArrayList<>();

	public void setBaseElementDefinitions(List<BaseElementDefinition> baseElementDefinitions) {
    this.baseElementDefinitions = baseElementDefinitions;
  }

  public List<BaseElementDefinition> getBaseElementDefinitions() {
		return baseElementDefinitions;
	}

	public void addBaseElementDefinition(BaseElementDefinition definition) {
		baseElementDefinitions.add(definition);
	}

	public static class BaseElementDefinition {
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
