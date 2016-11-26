# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

require_relative '../../../../ruby/spec_helper'

describe "Truffle::Interop.to_java_string" do

  it "can be round-tripped with from_java_string" do
    Truffle::Interop.from_java_string(Truffle::Interop.to_java_string("foo")).should == "foo"
  end

end
