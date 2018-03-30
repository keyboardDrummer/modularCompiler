package core.deltas

trait Contract {
  def dependencies: Set[Contract] = Set.empty

  override def toString: String = name
  def suffix: String
  def name: String = {
    try
    {
      val simpleName = getClass.getSimpleName
      var result = simpleName
      if (simpleName.endsWith("$"))
        result = simpleName.dropRight(1)
      if (result.endsWith(suffix))
        result = result.dropRight(suffix.length)
      splitCamelCase(result).toLowerCase
    }
    catch
    {
      case e: java.lang.InternalError => "internalError"
    }
  }

  def splitCamelCase(input: String): String = {
    input.replaceAll(
      String.format("%s|%s|%s",
        "(?<=[A-Z])(?=[A-Z][a-z])",
        "(?<=[^A-Z])(?=[A-Z])",
        "(?<=[A-Za-z])(?=[^A-Za-z])"
      ),
      " "
    )
  }
}