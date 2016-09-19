package nl.knaw.huygens.alexandria.endpoint.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeName;

import nl.knaw.huygens.alexandria.api.JsonTypeNames;
import nl.knaw.huygens.alexandria.api.model.ProcessStatus;

@JsonTypeName(JsonTypeNames.COMMANDSTATUS)
@JsonInclude(Include.NON_NULL)
public class CommandStatus extends ProcessStatus {

}
